variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "퍼블릭 서브넷 ID 목록"
  type        = list(string)
}

variable "backend_instance_type" {
  description = "백엔드 EC2 인스턴스 타입"
  type        = string
}

variable "frontend_instance_type" {
  description = "프론트엔드 EC2 인스턴스 타입"
  type        = string
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, staging, production)"
  type        = string
}
