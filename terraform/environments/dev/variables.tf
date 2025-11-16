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
  default     = "dev"
}

# Network
variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public Subnet CIDR 블록 리스트"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private Subnet CIDR 블록 리스트"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
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

# ALB
variable "enable_https" {
  description = "HTTPS 리스너 활성화 여부"
  type        = bool
  default     = false
}

variable "certificate_arn" {
  description = "ACM Certificate ARN (HTTPS 사용 시)"
  type        = string
  default     = ""
}

# Secrets Manager
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

# Docker MySQL (DEV 환경)
variable "mysql_host" {
  description = "MySQL 호스트 (Docker 사용 시 localhost)"
  type        = string
  default     = "localhost"
}

variable "mysql_port" {
  description = "MySQL 포트"
  type        = string
  default     = "3306"
}

variable "mysql_database" {
  description = "MySQL 데이터베이스명"
  type        = string
  default     = "coffeeshout_dev"
}

variable "mysql_username" {
  description = "MySQL 사용자명"
  type        = string
  default     = "coffeeshout"
}

variable "mysql_password" {
  description = "MySQL 비밀번호"
  type        = string
  sensitive   = true
}

# ElastiCache (DEV 환경)
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

# CloudWatch Logs
variable "log_retention_days" {
  description = "CloudWatch Logs 보존 기간 (일)"
  type        = number
  default     = 7
}

# Common Tags
variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default = {
    Project     = "CoffeeShout"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}
