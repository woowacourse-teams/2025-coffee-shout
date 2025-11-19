variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "s3_bucket_arn" {
  description = "S3 버킷 ARN"
  type        = string
}

variable "ssm_parameter_arns" {
  description = "SSM Parameter Store ARN 목록 (환경별 파라미터)"
  type        = list(string)
}

variable "sns_topic_arn" {
  description = "SNS Topic ARN (빌드 실패 알림용)"
  type        = string
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
