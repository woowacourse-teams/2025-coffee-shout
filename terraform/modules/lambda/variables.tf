variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "sns_topic_arn" {
  description = "SNS Topic ARN (CloudWatch 알람)"
  type        = string
}

variable "ssm_parameter_arns" {
  description = "SSM Parameter ARN 목록 (Lambda에서 읽기)"
  type        = list(string)
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}

variable "lambda_timeout" {
  description = "Lambda 함수 타임아웃 (초)"
  type        = number
  default     = 30
}

variable "lambda_memory_size" {
  description = "Lambda 함수 메모리 크기 (MB)"
  type        = number
  default     = 128
}
