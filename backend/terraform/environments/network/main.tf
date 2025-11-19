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
    key    = "terraform/tfstate/network/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "coffee-shout"
      Environment = "network"
      ManagedBy   = "Terraform"
    }
  }
}

locals {
  azs = ["ap-northeast-2a", "ap-northeast-2c"]
}

# 단일 VPC (Dev + Prod 공용)
module "vpc" {
  source = "../../modules/vpc"

  name_prefix        = "coffee-shout"
  vpc_cidr           = var.vpc_cidr
  availability_zones = local.azs

  # Public Subnets (Dev + Prod 공용)
  public_subnet_cidrs = [
    cidrsubnet(var.vpc_cidr, 4, 0), # 10.0.0.0/20 (ap-northeast-2a)
    cidrsubnet(var.vpc_cidr, 4, 1)  # 10.0.16.0/20 (ap-northeast-2c)
  ]

  # Private Subnets (RDS, ElastiCache 등)
  private_subnet_cidrs = [
    cidrsubnet(var.vpc_cidr, 4, 2), # 10.0.32.0/20 (ap-northeast-2a)
    cidrsubnet(var.vpc_cidr, 4, 3)  # 10.0.48.0/20 (ap-northeast-2c)
  ]
}

# Note: S3 bucket 'coffee-shout' is managed externally via setup-backend.sh
# This bucket is used for:
# - Terraform state files (terraform/tfstate/*)
# - QR codes (qr/dev/, qr/prod/)
