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
    key    = "terraform/tfstate/dev/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "coffee-shout"
      Environment = "dev"
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
  vpc_id            = data.terraform_remote_state.network.outputs.vpc_id
  public_subnet_ids = data.terraform_remote_state.network.outputs.public_subnet_ids
}

# Security Group
module "dev_sg" {
  source = "../../modules/security-group"

  name_prefix = "coffee-shout-dev"
  description = "Security group for Dev EC2"
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

# EC2 Instance
module "dev_ec2" {
  source = "../../modules/ec2"

  name               = "coffee-shout-dev"
  instance_type      = "t4g.small"
  subnet_id          = local.public_subnet_ids[0]
  security_group_ids = [module.dev_sg.security_group_id]
  key_name           = var.key_name

  user_data = templatefile("${path.module}/user-data.sh", {
    docker_compose_version = "2.24.5"
  })

  root_volume_size = 20
  root_volume_type = "gp3"

  tags = {
    Environment = "dev"
  }
}

# Elastic IP
resource "aws_eip" "dev" {
  instance = module.dev_ec2.instance_id
  domain   = "vpc"

  tags = {
    Name = "coffee-shout-dev-eip"
  }
}

# S3 Bucket is managed in network environment
# QR codes for dev are stored at: s3://coffee-shout/qr/dev/
