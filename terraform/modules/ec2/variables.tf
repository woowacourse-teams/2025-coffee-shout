variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "instance_name" {
  description = "인스턴스 이름 (예: backend-dev, backend-prod)"
  type        = string
}

variable "instance_type" {
  description = "EC2 인스턴스 타입"
  type        = string
  default     = "t4g.small"
}

variable "subnet_id" {
  description = "EC2를 배치할 Subnet ID"
  type        = string
}

variable "security_group_id" {
  description = "EC2 Security Group ID"
  type        = string
}

variable "iam_instance_profile_name" {
  description = "IAM Instance Profile 이름"
  type        = string
}

variable "parameter_path_prefix" {
  description = "SSM Parameter Store 경로 프리픽스 (예: /coffeeshout/prod)"
  type        = string
}

variable "root_volume_size" {
  description = "루트 볼륨 크기 (GB)"
  type        = number
  default     = 15
}

variable "assign_eip" {
  description = "Elastic IP 할당 여부"
  type        = bool
  default     = false
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
