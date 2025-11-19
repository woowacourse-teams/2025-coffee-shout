variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_ids" {
  description = "ALB를 배치할 Public Subnet IDs (최소 2개 AZ)"
  type        = list(string)
}

variable "security_group_id" {
  description = "ALB Security Group ID"
  type        = string
}

variable "target_instance_id" {
  description = "대상 EC2 인스턴스 ID"
  type        = string
}

variable "certificate_arn" {
  description = "ACM Certificate ARN (HTTPS 리스너용, 선택)"
  type        = string
  default     = ""
}

variable "enable_https" {
  description = "HTTPS 리스너 활성화 여부"
  type        = bool
  default     = false
}

variable "health_check_path" {
  description = "헬스체크 경로"
  type        = string
  default     = "/actuator/health"
}

variable "health_check_interval" {
  description = "헬스체크 간격 (초)"
  type        = number
  default     = 30
}

variable "health_check_timeout" {
  description = "헬스체크 타임아웃 (초)"
  type        = number
  default     = 5
}

variable "healthy_threshold" {
  description = "정상 판단 임계값"
  type        = number
  default     = 2
}

variable "unhealthy_threshold" {
  description = "비정상 판단 임계값"
  type        = number
  default     = 2
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
