variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
  default     = "coffeeshout"
}

variable "environment" {
  description = "환경"
  type        = string
  default     = "prod"
}

# Network
variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.1.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public Subnet CIDR 블록 리스트"
  type        = list(string)
  default     = ["10.1.1.0/24", "10.1.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private Subnet CIDR 블록 리스트"
  type        = list(string)
  default     = ["10.1.10.0/24", "10.1.11.0/24"]
}

variable "availability_zones" {
  description = "가용 영역 리스트"
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
}

# EC2
variable "instance_type" {
  description = "EC2 인스턴스 타입"
  type        = string
  default     = "t4g.small"
}

variable "root_volume_size" {
  description = "루트 볼륨 크기 (GB)"
  type        = number
  default     = 15
}

variable "assign_eip" {
  description = "Elastic IP 할당 여부"
  type        = bool
  default     = true
}

# ALB
variable "enable_https" {
  description = "HTTPS 리스너 활성화 여부"
  type        = bool
  default     = true
}

variable "certificate_arn" {
  description = "ACM Certificate ARN (HTTPS 사용 시 필수)"
  type        = string
}

# RDS
variable "rds_instance_class" {
  description = "RDS 인스턴스 클래스"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "RDS 할당 스토리지 (GB)"
  type        = number
  default     = 20
}

variable "rds_database_name" {
  description = "RDS 데이터베이스명"
  type        = string
  default     = "coffeeshout"
}

variable "rds_username" {
  description = "RDS 마스터 사용자명"
  type        = string
  default     = "admin"
}

variable "rds_backup_retention_period" {
  description = "RDS 백업 보존 기간 (일)"
  type        = number
  default     = 7
}

# ElastiCache
variable "elasticache_node_type" {
  description = "ElastiCache 노드 타입"
  type        = string
  default     = "cache.t3.micro"
}

variable "elasticache_engine_version" {
  description = "ElastiCache 엔진 버전 (Valkey)"
  type        = string
  default     = "8.0"
}

# SSM Parameter Store (Secrets Manager 대체)
variable "tempo_url" {
  description = "Tempo URL"
  type        = string
  default     = "http://43.202.22.216/tempo/v1/traces"
}

variable "trace_sampling_probability" {
  description = "Trace Sampling Probability"
  type        = string
  default     = "0.1"
}

# Slack 알림
variable "slack_bot_token" {
  description = "Slack Bot Token (Lambda 알림용)"
  type        = string
  sensitive   = true
}

variable "slack_channel" {
  description = "Slack 채널 (알림 전송용, 예: #aws-알림)"
  type        = string
  default     = "#aws-알림"
}

# CI/CD Pipeline
variable "github_connection_arn" {
  description = "GitHub CodeStar Connection ARN (AWS 콘솔에서 미리 생성 필요)"
  type        = string
}

variable "github_repo" {
  description = "GitHub 리포지토리 (예: woowacourse-teams/2025-coffee-shout)"
  type        = string
  default     = "woowacourse-teams/2025-coffee-shout"
}

variable "github_branch" {
  description = "GitHub 배포 브랜치"
  type        = string
  default     = "main"
}

# CloudWatch Logs
variable "log_retention_days" {
  description = "CloudWatch Logs 보존 기간 (일)"
  type        = number
  default     = 30
}

# Common Tags
variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default = {
    Project     = "CoffeeShout"
    Environment = "prod"
    ManagedBy   = "Terraform"
  }
}
