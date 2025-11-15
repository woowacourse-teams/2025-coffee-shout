# Terraform 버전 및 Provider 설정
terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Terraform 상태 파일을 저장할 백엔드 설정 (나중에 S3로 변경 가능)
  # backend "s3" {
  #   bucket = "커피빵-terraform-state"
  #   key    = "terraform.tfstate"
  #   region = "ap-northeast-2"
  # }
}

# AWS Provider 설정
provider "aws" {
  region = var.aws_region

  # 모든 리소스에 공통으로 적용될 태그
  default_tags {
    tags = {
      Project     = "CoffeeShout"
      ManagedBy   = "Terraform"
      Environment = var.environment
    }
  }
}
