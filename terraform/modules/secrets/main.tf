# ========================================
# SSM Parameter Store (완전 무료, Secrets Manager 대체)
# ========================================
# Secrets Manager: $0.40/월 → SSM Parameter Store: 무료!
# SecureString: KMS 암호화 (추가 비용 없음)
# ========================================

# S3 설정
resource "aws_ssm_parameter" "s3_bucket_name" {
  name        = "/${var.project_name}/${var.environment}/s3-bucket-name"
  description = "S3 버킷 이름"
  type        = "String"
  value       = var.s3_bucket_name

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-s3-bucket-name"
    }
  )
}

resource "aws_ssm_parameter" "s3_qr_key_prefix" {
  name        = "/${var.project_name}/${var.environment}/s3-qr-key-prefix"
  description = "S3 QR 코드 키 프리픽스"
  type        = "String"
  value       = "qr-code/${var.environment}/"

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-s3-qr-key-prefix"
    }
  )
}

# 환경
resource "aws_ssm_parameter" "environment" {
  name        = "/${var.project_name}/${var.environment}/environment"
  description = "환경 (dev, prod)"
  type        = "String"
  value       = var.environment

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-environment"
    }
  )
}

# Redis (ElastiCache) 설정
resource "aws_ssm_parameter" "redis_host" {
  name        = "/${var.project_name}/${var.environment}/redis-host"
  description = "Redis 호스트 주소"
  type        = "String"
  value       = var.redis_host

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-redis-host"
    }
  )
}

resource "aws_ssm_parameter" "redis_port" {
  name        = "/${var.project_name}/${var.environment}/redis-port"
  description = "Redis 포트"
  type        = "String"
  value       = tostring(var.redis_port)

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-redis-port"
    }
  )
}

# Grafana Tempo (분산 추적)
resource "aws_ssm_parameter" "tempo_url" {
  name        = "/${var.project_name}/${var.environment}/tempo-url"
  description = "Grafana Tempo URL"
  type        = "String"
  value       = var.tempo_url

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-tempo-url"
    }
  )
}

resource "aws_ssm_parameter" "trace_sampling_probability" {
  name        = "/${var.project_name}/${var.environment}/trace-sampling-probability"
  description = "추적 샘플링 확률"
  type        = "String"
  value       = var.trace_sampling_probability

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-trace-sampling-probability"
    }
  )
}

# MySQL (RDS) 설정 - SecureString (암호화)
resource "aws_ssm_parameter" "mysql_url" {
  name        = "/${var.project_name}/${var.environment}/mysql-url"
  description = "MySQL JDBC URL"
  type        = "SecureString" # KMS 암호화
  value       = var.mysql_url

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-mysql-url"
    }
  )
}

resource "aws_ssm_parameter" "mysql_username" {
  name        = "/${var.project_name}/${var.environment}/mysql-username"
  description = "MySQL 사용자 이름"
  type        = "SecureString" # KMS 암호화
  value       = var.mysql_username

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-mysql-username"
    }
  )
}

resource "aws_ssm_parameter" "mysql_password" {
  name        = "/${var.project_name}/${var.environment}/mysql-password"
  description = "MySQL 비밀번호"
  type        = "SecureString" # KMS 암호화
  value       = var.mysql_password

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-mysql-password"
    }
  )
}

# Slack 알림 설정
resource "aws_ssm_parameter" "slack_bot_token" {
  name        = "/${var.project_name}/${var.environment}/slack-bot-token"
  description = "Slack Bot Token (Lambda 알림용)"
  type        = "SecureString" # KMS 암호화
  value       = var.slack_bot_token

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-slack-bot-token"
    }
  )
}

resource "aws_ssm_parameter" "slack_channel" {
  name        = "/${var.project_name}/${var.environment}/slack-channel"
  description = "Slack 채널 (알림 전송용)"
  type        = "String"
  value       = var.slack_channel

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-slack-channel"
    }
  )
}
