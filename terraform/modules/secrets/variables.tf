variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

# S3 관련
variable "s3_bucket_name" {
  description = "S3 버킷 이름"
  type        = string
}

# Redis (ElastiCache) 관련
variable "redis_host" {
  description = "Redis 호스트 주소"
  type        = string
}

variable "redis_port" {
  description = "Redis 포트"
  type        = number
  default     = 6379
}

# Grafana Tempo 관련
variable "tempo_url" {
  description = "Grafana Tempo URL"
  type        = string
}

variable "trace_sampling_probability" {
  description = "추적 샘플링 확률 (0.0 ~ 1.0)"
  type        = string
  default     = "0.1"
}

# MySQL (RDS) 관련
variable "mysql_url" {
  description = "MySQL 엔드포인트"
  type        = string
  sensitive   = true
}

variable "mysql_username" {
  description = "MySQL 사용자 이름"
  type        = string
  sensitive   = true
}

variable "mysql_password" {
  description = "MySQL 비밀번호"
  type        = string
  sensitive   = true
}

# Slack 알림 관련
variable "slack_bot_token" {
  description = "Slack Bot Token (Lambda 알림용)"
  type        = string
  sensitive   = true
}

variable "slack_channel" {
  description = "Slack 채널 (예: #aws-알림)"
  type        = string
  default     = "#aws-알림"
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
