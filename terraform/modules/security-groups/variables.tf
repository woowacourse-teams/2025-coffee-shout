variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "ssh_allowed_cidrs" {
  description = "SSH 접근을 허용할 CIDR 블록 목록"
  type        = list(string)
  default     = ["0.0.0.0/0"] # 운영 환경에서는 특정 IP로 제한 필요
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
