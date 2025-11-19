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
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.4"
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
# RDS (PROD 전용)
# ========================================

module "rds" {
  source = "../../modules/rds"

  project_name            = var.project_name
  environment             = var.environment
  instance_class          = var.rds_instance_class
  allocated_storage       = var.rds_allocated_storage
  database_name           = var.rds_database_name
  master_username         = var.rds_username
  subnet_ids              = module.network.private_subnet_ids # Private Subnet에 배치
  security_group_id       = module.security_groups.rds_security_group_id
  backup_retention_period = var.rds_backup_retention_period
  log_retention_days      = var.log_retention_days
  common_tags             = var.common_tags
}

# ========================================
# ElastiCache (PROD 전용)
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
# SSM Parameter Store (무료, Secrets Manager 대체)
# ========================================

module "secrets" {
  source = "../../modules/secrets"

  project_name               = var.project_name
  environment                = var.environment
  s3_bucket_name             = module.s3.bucket_name
  redis_host                 = module.elasticache.host
  tempo_url                  = var.tempo_url
  trace_sampling_probability = var.trace_sampling_probability
  mysql_url                  = "jdbc:mysql://${module.rds.endpoint}/${var.rds_database_name}"
  mysql_username             = var.rds_username
  mysql_password             = module.rds.password
  slack_bot_token            = var.slack_bot_token
  slack_channel              = var.slack_channel
  common_tags                = var.common_tags
}

# ========================================
# IAM
# ========================================

module "iam" {
  source = "../../modules/iam"

  project_name       = var.project_name
  environment        = var.environment
  ssm_parameter_arns = module.secrets.parameter_arns
  s3_bucket_arn      = module.s3.bucket_arn
  sns_topic_arn      = module.sns.topic_arn
  common_tags        = var.common_tags
}

# ========================================
# EC2
# ========================================

module "ec2" {
  source = "../../modules/ec2"

  project_name              = var.project_name
  environment               = var.environment
  instance_name             = "backend-prod"
  instance_type             = var.instance_type
  subnet_id                 = module.network.public_subnet_ids[0]
  security_group_id         = module.security_groups.ec2_security_group_id
  iam_instance_profile_name = module.iam.ec2_instance_profile_name
  parameter_path_prefix     = module.secrets.parameter_path_prefix
  root_volume_size          = var.root_volume_size
  assign_eip                = var.assign_eip
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

# ========================================
# SNS Topic (CloudWatch 알람용)
# ========================================

module "sns" {
  source = "../../modules/sns"

  project_name = var.project_name
  environment  = var.environment
  common_tags  = var.common_tags
}

# ========================================
# Lambda (Slack 알림)
# ========================================

module "lambda" {
  source = "../../modules/lambda"

  project_name       = var.project_name
  environment        = var.environment
  sns_topic_arn      = module.sns.topic_arn
  ssm_parameter_arns = module.secrets.parameter_arns
  common_tags        = var.common_tags
}

# ========================================
# CloudWatch 모니터링 (무료 티어 - 알람 8개)
# ========================================

module "monitoring" {
  source = "../../modules/monitoring"

  project_name                = var.project_name
  environment                 = var.environment
  ec2_instance_id             = module.ec2.instance_id
  rds_instance_id             = module.rds.instance_id
  alb_arn_suffix              = module.alb.alb_arn_suffix
  alb_target_group_arn_suffix = module.alb.target_group_arn_suffix
  elasticache_cluster_id      = module.elasticache.cluster_id
  sns_topic_arn               = module.sns.topic_arn
  common_tags                 = var.common_tags
}

# ========================================
# CI/CD Pipeline
# ========================================

# CodeBuild (무료 티어: 월 100분)
module "codebuild" {
  source = "../../modules/codebuild"

  project_name       = var.project_name
  environment        = var.environment
  codebuild_role_arn = module.iam.codebuild_role_arn
  s3_bucket_name     = module.s3.bucket_name
  sns_topic_arn      = module.sns.topic_arn
  github_repo        = var.github_repo
  github_branch      = var.github_branch
  common_tags        = var.common_tags
}

# CodeDeploy (EC2 배포 무료)
module "codedeploy" {
  source = "../../modules/codedeploy"

  project_name         = var.project_name
  environment          = var.environment
  codedeploy_role_arn  = module.iam.codedeploy_role_arn
  ec2_instance_ids     = [module.ec2.instance_id]
  common_tags          = var.common_tags
}

# CodePipeline (무료 티어: 월 1개 파이프라인)
module "codepipeline" {
  source = "../../modules/codepipeline"

  project_name                     = var.project_name
  environment                      = var.environment
  s3_bucket_name                   = module.s3.bucket_name
  github_connection_arn            = var.github_connection_arn
  github_repo                      = var.github_repo
  github_branch                    = var.github_branch
  codebuild_project_name           = module.codebuild.project_name
  codedeploy_app_name              = module.codedeploy.app_name
  codedeploy_deployment_group_name = module.codedeploy.deployment_group_name
  common_tags                      = var.common_tags
}

