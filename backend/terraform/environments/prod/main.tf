terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket = "coffee-shout"
    key    = "terraform/tfstate/prod/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "coffee-shout"
      Environment = "prod"
      ManagedBy   = "Terraform"
    }
  }
}

# Network state에서 VPC 정보 가져오기
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "coffee-shout"
    key    = "terraform/tfstate/network/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

locals {
  vpc_id             = data.terraform_remote_state.network.outputs.vpc_id
  public_subnet_ids  = data.terraform_remote_state.network.outputs.public_subnet_ids
  private_subnet_ids = data.terraform_remote_state.network.outputs.private_subnet_ids
}

# Security Groups
module "prod_ec2_sg" {
  source = "../../modules/security-group"

  name_prefix = "coffee-shout-prod-ec2"
  description = "Security group for Prod EC2"
  vpc_id      = local.vpc_id

  ingress_rules = [
    {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "SSH"
    },
    {
      from_port   = 80
      to_port     = 80
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "HTTP"
    },
    {
      from_port   = 443
      to_port     = 443
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "HTTPS"
    },
    {
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Application"
    }
  ]
}

module "prod_rds_sg" {
  source = "../../modules/security-group"

  name_prefix = "coffee-shout-prod-rds"
  description = "Security group for Prod RDS"
  vpc_id      = local.vpc_id

  ingress_rules = [
    {
      from_port                = 3306
      to_port                  = 3306
      protocol                 = "tcp"
      cidr_blocks              = []
      source_security_group_id = module.prod_ec2_sg.security_group_id
      description              = "MySQL from EC2"
    }
  ]
}

module "prod_elasticache_sg" {
  source = "../../modules/security-group"

  name_prefix = "coffee-shout-prod-valkey"
  description = "Security group for Prod ElastiCache (Valkey)"
  vpc_id      = local.vpc_id

  ingress_rules = [
    {
      from_port                = 6379
      to_port                  = 6379
      protocol                 = "tcp"
      cidr_blocks              = []
      source_security_group_id = module.prod_ec2_sg.security_group_id
      description              = "Valkey from EC2"
    }
  ]
}

# EC2 Instance
module "prod_ec2" {
  source = "../../modules/ec2"

  name               = "coffee-shout-prod"
  instance_type      = "t4g.small"
  subnet_id          = local.public_subnet_ids[0]
  security_group_ids = [module.prod_ec2_sg.security_group_id]
  key_name           = var.key_name

  user_data = file("${path.module}/user-data.sh")

  root_volume_size = 20
  root_volume_type = "gp3"

  tags = {
    Environment = "prod"
  }
}

# Elastic IP
resource "aws_eip" "prod" {
  instance = module.prod_ec2.instance_id
  domain   = "vpc"

  tags = {
    Name = "coffee-shout-prod-eip"
  }
}

# RDS (MySQL, Free Tier)
module "prod_rds" {
  source = "../../modules/rds"

  name_prefix = "coffee-shout-prod"

  # Free tier configuration
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = "db.t4g.micro"

  # Free tier storage (max 20GB)
  allocated_storage = 20
  storage_type      = "gp3"

  # Database configuration
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 3306

  # Network configuration
  subnet_ids         = local.private_subnet_ids
  security_group_ids = [module.prod_rds_sg.security_group_id]
  publicly_accessible = false

  # Backup configuration
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  # Free tier requires single-AZ
  multi_az = false

  # Production settings
  skip_final_snapshot = false
  deletion_protection = true
  apply_immediately   = false

  tags = {
    Environment = "prod"
  }
}

# ElastiCache (Valkey, Free Tier)
module "prod_elasticache" {
  source = "../../modules/elasticache"

  name_prefix = "coffee-shout-prod"

  # Free tier configuration
  engine         = "valkey"
  engine_version = "7.2"
  node_type      = "cache.t4g.micro"
  num_cache_nodes = 1
  port           = 6379

  # Network configuration
  subnet_ids         = local.private_subnet_ids
  security_group_ids = [module.prod_elasticache_sg.security_group_id]

  # Maintenance
  maintenance_window       = "sun:05:00-sun:06:00"
  snapshot_retention_limit = 5
  snapshot_window          = "03:00-05:00"

  tags = {
    Environment = "prod"
  }
}

# S3 Bucket is managed externally via setup-backend.sh
# QR codes for prod are stored at: s3://coffee-shout/qr/prod/
