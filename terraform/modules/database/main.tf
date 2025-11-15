# 데이터베이스 서브넷 그룹
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}

# 데이터베이스 보안 그룹
resource "aws_security_group" "database" {
  name        = "${var.project_name}-${var.environment}-db-sg"
  description = "Security group for RDS database"
  vpc_id      = var.vpc_id

  # MySQL/MariaDB 포트 (백엔드 서버에서만 접근 가능)
  ingress {
    description     = "MySQL from backend"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.backend_security_group_id]
  }

  # PostgreSQL 포트 (필요시 사용)
  ingress {
    description     = "PostgreSQL from backend"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [var.backend_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-sg"
  }
}

# RDS 인스턴스 (MySQL)
resource "aws_db_instance" "main" {
  identifier = "${var.project_name}-${var.environment}-db"

  # 엔진 설정
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.db_instance_class

  # 스토리지 설정
  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp3"
  storage_encrypted     = true

  # 데이터베이스 설정
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  # 네트워크 설정
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.database.id]
  publicly_accessible    = false

  # 백업 설정
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  # 고가용성 설정 (운영 환경에서는 true로 설정)
  multi_az = var.environment == "production" ? true : false

  # 삭제 보호 (운영 환경에서는 true로 설정)
  deletion_protection = var.environment == "production" ? true : false
  skip_final_snapshot = var.environment != "production"

  # 파라미터 그룹 (문자셋 설정)
  parameter_group_name = aws_db_parameter_group.main.name

  # 로그 설정
  enabled_cloudwatch_logs_exports = ["error", "general", "slowquery"]

  tags = {
    Name = "${var.project_name}-${var.environment}-db"
  }
}

# 데이터베이스 파라미터 그룹 (한글 지원 설정)
resource "aws_db_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-db-params"
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

  # 시간대 설정
  parameter {
    name  = "time_zone"
    value = "Asia/Seoul"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-params"
  }
}
