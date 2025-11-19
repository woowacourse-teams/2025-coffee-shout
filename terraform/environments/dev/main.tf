terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = var.common_tags
  }
}

# ========================================
# Network
# ========================================

module "network" {
  source = "../../modules/network"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs # RDS, ElastiCache용 Private Subnet
  availability_zones   = var.availability_zones
  enable_nat_gateway   = false # 비용 절감 (RDS/ElastiCache는 인터넷 접근 불필요)
  common_tags          = var.common_tags
}

# ========================================
# Security Groups
# ========================================

module "security_groups" {
  source = "../../modules/security-groups"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.network.vpc_id
  common_tags  = var.common_tags
}

# ========================================
# S3
# ========================================

module "s3" {
  source = "../../modules/s3"

  project_name = var.project_name
  environment  = var.environment
  common_tags  = var.common_tags
}

# ========================================
# ElastiCache (DEV 환경)
# ========================================

module "elasticache" {
  source = "../../modules/elasticache"

  project_name      = var.project_name
  environment       = var.environment
  node_type         = var.elasticache_node_type
  engine_version    = var.elasticache_engine_version
  subnet_ids        = module.network.private_subnet_ids # Private Subnet에 배치
  security_group_id = module.security_groups.elasticache_security_group_id
  common_tags       = var.common_tags
}

# ========================================
# Secrets Manager
# ========================================

module "secrets" {
  source = "../../modules/secrets"

  project_name               = var.project_name
  environment                = var.environment
  s3_bucket_name             = module.s3.bucket_name
  redis_host                 = module.elasticache.endpoint
  tempo_url                  = var.tempo_url
  trace_sampling_probability = var.trace_sampling_probability
  mysql_url                  = "jdbc:mysql://${var.mysql_host}:${var.mysql_port}/${var.mysql_database}"
  mysql_username             = var.mysql_username
  mysql_password             = var.mysql_password
  common_tags                = var.common_tags
  slack_bot_token = var.slack_bot_token
}

# ========================================
# IAM
# ========================================

module "iam" {
  source = "../../modules/iam"

  project_name = var.project_name
  environment  = var.environment
  # SSM Parameter Store ARNs (환경별 설정)
  ssm_parameter_arns = [
    "arn:aws:ssm:${var.aws_region}:*:parameter/${var.project_name}/${var.environment}/*"
  ]
  # SNS Topic ARN (빌드 실패 알림용)
  sns_topic_arn = "arn:aws:sns:${var.aws_region}:*:${var.project_name}-${var.environment}-alerts"
  s3_bucket_arn = module.s3.bucket_arn
  common_tags   = var.common_tags
}

# ========================================
# EC2
# ========================================

module "ec2" {
  source = "../../modules/ec2"

  project_name              = var.project_name
  environment               = var.environment
  instance_name             = "backend-dev"
  instance_type             = var.instance_type
  subnet_id                 = module.network.public_subnet_ids[0]
  security_group_id         = module.security_groups.ec2_security_group_id
  iam_instance_profile_name = module.iam.ec2_instance_profile_name
  parameter_path_prefix     = "/${var.project_name}/${var.environment}"
  root_volume_size          = var.root_volume_size
  assign_eip                = false # DEV는 EIP 미사용
  common_tags               = var.common_tags
}

# ========================================
# ALB
# ========================================

module "alb" {
  source = "../../modules/alb"

  project_name       = var.project_name
  environment        = var.environment
  vpc_id             = module.network.vpc_id
  subnet_ids         = module.network.public_subnet_ids
  security_group_id  = module.security_groups.alb_security_group_id
  target_instance_id = module.ec2.instance_id
  certificate_arn    = var.certificate_arn
  enable_https       = var.enable_https
  common_tags        = var.common_tags
}
