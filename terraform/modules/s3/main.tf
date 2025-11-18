# ========================================
# Local Variables
# ========================================

locals {
  # bucket_name이 지정되지 않으면 자동 생성
  bucket_name = var.bucket_name != "" ? var.bucket_name : "${var.project_name}-${var.environment}-bucket"
}

# ========================================
# S3 Bucket
# ========================================

# 메인 S3 버킷
resource "aws_s3_bucket" "main" {
  bucket = local.bucket_name

  tags = merge(
    var.common_tags,
    {
      Name = local.bucket_name
    }
  )
}

# 버저닝 활성화 (버전 관리)
resource "aws_s3_bucket_versioning" "main" {
  bucket = aws_s3_bucket.main.id

  versioning_configuration {
    status = "Enabled"
  }
}

# 서버 측 암호화 (SSE-S3)
resource "aws_s3_bucket_server_side_encryption_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Public Access Block (보안 강화)
resource "aws_s3_bucket_public_access_block" "main" {
  bucket = aws_s3_bucket.main.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# CORS 설정 (QR 코드 이미지 접근용)
resource "aws_s3_bucket_cors_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = var.cors_allowed_origins
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

# Lifecycle 정책 (오래된 파일 자동 삭제)
resource "aws_s3_bucket_lifecycle_configuration" "main" {
  bucket = aws_s3_bucket.main.id

  # QR 코드 이미지 정리 (90일 후 삭제)
  rule {
    id     = "delete-old-qr-codes"
    status = "Enabled"

    filter {
      prefix = "qr-code/"
    }

    expiration {
      days = 90
    }
  }

  # CodeDeploy 아티팩트 정리 (30일 후 삭제)
  rule {
    id     = "delete-old-codedeploy-artifacts"
    status = "Enabled"

    filter {
      prefix = "codedeploy/artifacts/"
    }

    expiration {
      days = 30
    }
  }

  # 미완료 멀티파트 업로드 정리 (7일 후)
  rule {
    id     = "abort-incomplete-multipart-upload"
    status = "Enabled"

    filter {} # 모든 객체에 적용

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}
