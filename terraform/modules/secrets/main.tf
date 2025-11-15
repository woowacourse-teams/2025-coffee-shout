# 애플리케이션 환경 변수를 저장하는 Secrets Manager
resource "aws_secretsmanager_secret" "app_config" {
  name        = "${var.project_name}/${var.environment}/app-config"
  description = "Application configuration for ${var.project_name} ${var.environment} environment"

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-app-config"
    }
  )
}

# 모든 환경 변수를 JSON으로 저장
resource "aws_secretsmanager_secret_version" "app_config" {
  secret_id = aws_secretsmanager_secret.app_config.id

  secret_string = jsonencode({
    # S3 설정
    S3_BUCKET_NAME   = var.s3_bucket_name
    S3_QR_KEY_PREFIX = "qr-code/${var.environment}/"

    # 환경
    ENVIRONMENT = var.environment

    # Redis (ElastiCache)
    REDIS_HOST = var.redis_host
    REDIS_PORT = var.redis_port

    # Grafana Tempo (분산 추적)
    TEMPO_URL                  = var.tempo_url
    TRACE_SAMPLING_PROBABILITY = var.trace_sampling_probability

    # MySQL (RDS)
    MYSQL_URL      = var.mysql_url
    MYSQL_USERNAME = var.mysql_username
    MYSQL_PASSWORD = var.mysql_password

    # Spring Boot JDBC URL (선택적)
    JDBC_URL = var.jdbc_url
  })
}
