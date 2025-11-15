# Terraform State 저장용 S3 + DynamoDB 부트스트랩
# 이 파일은 최초 1회만 실행하여 백엔드 인프라를 생성합니다

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
  region = "ap-northeast-2"
}

# Terraform State 저장용 S3 버킷
resource "aws_s3_bucket" "terraform_state" {
  bucket = "coffeeshout-terraform-state"

  tags = {
    Name        = "Terraform State Bucket"
    Project     = "CoffeeShout"
    Environment = "Shared"
    ManagedBy   = "Terraform"
  }
}

# S3 버킷 버저닝 활성화 (State 파일 이력 관리)
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  versioning_configuration {
    status = "Enabled"
  }
}

# S3 버킷 암호화 활성화
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# S3 버킷 퍼블릭 액세스 차단
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Terraform State Lock용 DynamoDB 테이블
resource "aws_dynamodb_table" "terraform_locks" {
  name         = "coffeeshout-terraform-locks"
  billing_mode = "PAY_PER_REQUEST" # 프리티어: 25 읽기/쓰기 유닛까지 무료
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name        = "Terraform Lock Table"
    Project     = "CoffeeShout"
    Environment = "Shared"
    ManagedBy   = "Terraform"
  }
}

# 출력값
output "s3_bucket_name" {
  description = "Terraform State를 저장할 S3 버킷 이름"
  value       = aws_s3_bucket.terraform_state.id
}

output "dynamodb_table_name" {
  description = "Terraform Lock을 위한 DynamoDB 테이블 이름"
  value       = aws_dynamodb_table.terraform_locks.name
}

output "backend_config" {
  description = "백엔드 설정 정보 (이 값을 복사해서 사용하세요)"
  value = <<-EOT

    terraform {
      backend "s3" {
        bucket         = "${aws_s3_bucket.terraform_state.id}"
        key            = "ENV_NAME/terraform.tfstate"  # ENV_NAME을 dev 또는 prod로 변경
        region         = "ap-northeast-2"
        dynamodb_table = "${aws_dynamodb_table.terraform_locks.name}"
        encrypt        = true
      }
    }
  EOT
}
