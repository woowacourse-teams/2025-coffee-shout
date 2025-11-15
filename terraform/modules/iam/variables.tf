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

variable "secrets_manager_arn" {
  description = "Secrets Manager ARN (환경별 시크릿)"
  type        = string
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
