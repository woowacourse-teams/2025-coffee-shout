variable "bucket_name" {
  description = "S3 버킷 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "cors_allowed_origins" {
  description = "CORS 허용 오리진 목록"
  type        = list(string)
  default     = ["*"] # 운영 환경에서는 실제 도메인으로 제한
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
