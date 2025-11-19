variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "instance_class" {
  description = "RDS 인스턴스 클래스"
  type        = string
  default     = "db.t3.micro"
}

variable "allocated_storage" {
  description = "할당된 스토리지 크기 (GB)"
  type        = number
  default     = 20 # 프리티어 한도
}

variable "database_name" {
  description = "데이터베이스 이름"
  type        = string
  default     = "coffeeshout"
}

variable "master_username" {
  description = "데이터베이스 마스터 사용자 이름"
  type        = string
  default     = "admin"
}

variable "subnet_ids" {
  description = "Subnet ID 목록 (RDS 배치용, Private Subnet)"
  type        = list(string)
}

variable "security_group_id" {
  description = "RDS Security Group ID"
  type        = string
}

variable "backup_retention_period" {
  description = "백업 보관 기간 (일)"
  type        = number
  default     = 7
}

variable "log_retention_days" {
  description = "CloudWatch Logs 보관 기간 (일)"
  type        = number
  default     = 7 # 개발: 7일, 운영: 30일 권장
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
