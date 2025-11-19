# ========================================
# Terraform State Backend Bootstrap
# ========================================
# 이 파일은 Terraform state를 저장할 S3 버킷을 생성합니다.
# Terraform 1.10+부터 S3 네이티브 locking을 지원하므로
# DynamoDB 테이블이 필요하지 않습니다.
#
# 사용법:
# 1. cd terraform/bootstrap
# 2. terraform init
# 3. terraform apply
# 4. 생성된 S3 버킷으로 각 환경의 backend.tf 설정
# ========================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ========================================
# S3 Buckets for Terraform State
# ========================================

# Dev 환경 S3 버킷
resource "aws_s3_bucket" "terraform_state_dev" {
  bucket = "coffeeshout-terraform-state-dev"

  tags = {
    Name        = "Terraform State - Dev"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}

# Prod 환경 S3 버킷
resource "aws_s3_bucket" "terraform_state_prod" {
  bucket = "coffeeshout-terraform-state-prod"

  tags = {
    Name        = "Terraform State - Prod"
    Environment = "prod"
    ManagedBy   = "Terraform"
  }
}

# Dev 버킷 버저닝 활성화
resource "aws_s3_bucket_versioning" "dev" {
  bucket = aws_s3_bucket.terraform_state_dev.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Prod 버킷 버저닝 활성화
resource "aws_s3_bucket_versioning" "prod" {
  bucket = aws_s3_bucket.terraform_state_prod.id

  versioning_configuration {
    status = "Enabled"
  }
}

# Dev 버킷 암호화
resource "aws_s3_bucket_server_side_encryption_configuration" "dev" {
  bucket = aws_s3_bucket.terraform_state_dev.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Prod 버킷 암호화
resource "aws_s3_bucket_server_side_encryption_configuration" "prod" {
  bucket = aws_s3_bucket.terraform_state_prod.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Dev 버킷 Public Access Block
resource "aws_s3_bucket_public_access_block" "dev" {
  bucket = aws_s3_bucket.terraform_state_dev.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Prod 버킷 Public Access Block
resource "aws_s3_bucket_public_access_block" "prod" {
  bucket = aws_s3_bucket.terraform_state_prod.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ========================================
# S3 Native State Locking
# ========================================
# Terraform 1.10+는 S3 conditional writes를 사용하여
# DynamoDB 없이 state locking을 지원합니다.
# backend 설정에서 use_lockfile = true 사용
# ========================================
