variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev/prod)"
  type        = string
}

variable "ec2_instance_id" {
  description = "모니터링할 EC2 인스턴스 ID"
  type        = string
}

variable "rds_instance_id" {
  description = "모니터링할 RDS 인스턴스 ID"
  type        = string
}

variable "alb_arn_suffix" {
  description = "모니터링할 ALB ARN Suffix"
  type        = string
}

variable "alb_target_group_arn_suffix" {
  description = "모니터링할 ALB Target Group ARN Suffix"
  type        = string
}

variable "elasticache_cluster_id" {
  description = "모니터링할 ElastiCache 클러스터 ID"
  type        = string
}

variable "sns_topic_arn" {
  description = "SNS Topic ARN (알람 알림용)"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

# Alarm Thresholds
variable "ec2_status_check_threshold" {
  description = "EC2 상태 체크 실패 임계값"
  type        = number
  default     = 1
}

variable "rds_cpu_threshold" {
  description = "RDS CPU 사용률 임계값 (%)"
  type        = number
  default     = 80
}

variable "rds_storage_threshold" {
  description = "RDS 사용 가능한 스토리지 임계값 (bytes)"
  type        = number
  default     = 2147483648 # 2GB
}

variable "rds_connections_threshold" {
  description = "RDS 연결 수 임계값"
  type        = number
  default     = 80
}

variable "alb_unhealthy_host_threshold" {
  description = "ALB Unhealthy 호스트 수 임계값"
  type        = number
  default     = 1
}

variable "elasticache_cpu_threshold" {
  description = "ElastiCache CPU 사용률 임계값 (%)"
  type        = number
  default     = 75
}

variable "elasticache_memory_threshold" {
  description = "ElastiCache 메모리 사용률 임계값 (%)"
  type        = number
  default     = 90
}
