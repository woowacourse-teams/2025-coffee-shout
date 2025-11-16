# RDS용 랜덤 비밀번호 생성
resource "random_password" "db_password" {
  length  = 16
  special = true
  # 특수문자 제한 (RDS에서 문제 일으킬 수 있는 문자 제외)
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# RDS 비밀번호를 Secrets Manager에 저장
resource "aws_secretsmanager_secret" "db_password" {
  name        = "${var.project_name}/${var.environment}/rds-password"
  description = "RDS MySQL master password for ${var.environment}"

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-password"
    }
  )
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db_password.result
}

# DB Subnet Group (Private Subnet에 배치)
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-db-subnet-group"
    }
  )
}

# DB Parameter Group (한글 지원 UTF-8 설정)
resource "aws_db_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-mysql80"
  family = "mysql8.0"

  # UTF-8 문자셋 설정 (한글 지원)
  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_client"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_connection"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_database"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_results"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_connection"
    value = "utf8mb4_unicode_ci"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  # 시간대 설정 (서울)
  parameter {
    name  = "time_zone"
    value = "Asia/Seoul"
  }

  # 슬로우 쿼리 로그 (성능 모니터링)
  parameter {
    name  = "slow_query_log"
    value = "1"
  }

  parameter {
    name  = "long_query_time"
    value = "2"
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-mysql80-params"
    }
  )
}

# RDS MySQL Instance
resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-${var.environment}-mysql"

  # 엔진 설정
  engine         = "mysql"
  engine_version = "8.0.43"
  instance_class = var.instance_class

  # 스토리지 설정
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.allocated_storage # Auto Scaling 비활성화 (비용 방지)
  storage_type          = "gp2"
  storage_encrypted     = true

  # 데이터베이스 설정
  db_name  = var.database_name
  username = var.master_username
  password = random_password.db_password.result

  # 네트워크 설정
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false
  port                   = 3306

  # Parameter Group
  parameter_group_name = aws_db_parameter_group.main.name

  # 백업 설정
  backup_retention_period = var.backup_retention_period
  backup_window           = "03:00-04:00" # 새벽 3-4시 (한국 시간 기준)
  maintenance_window      = "mon:04:00-mon:05:00"

  # 고가용성 설정 (프리티어에서는 Single-AZ)
  multi_az = false

  # 삭제 보호
  deletion_protection       = var.environment == "prod" ? true : false
  skip_final_snapshot       = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}" : null

  # 마이너 버전 자동 업그레이드
  auto_minor_version_upgrade = true

  # CloudWatch Logs 내보내기
  enabled_cloudwatch_logs_exports = ["error", "general", "slowquery"]

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-mysql"
    }
  )
}
