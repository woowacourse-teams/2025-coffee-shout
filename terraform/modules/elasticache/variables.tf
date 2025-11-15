variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "cache_node_type" {
  description = "ElastiCache 노드 타입"
  type        = string
  default     = "cache.t3.micro"
}

variable "private_subnet_ids" {
  description = "Private Subnet ID 목록 (ElastiCache 배치용)"
  type        = list(string)
}

variable "cache_security_group_id" {
  description = "ElastiCache Security Group ID"
  type        = string
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
