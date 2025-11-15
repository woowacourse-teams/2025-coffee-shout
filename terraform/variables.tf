# 기본 설정 변수들

variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2" # 서울 리전
}

variable "environment" {
  description = "환경 (dev, staging, production)"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
  default     = "coffeeshout"
}

# 네트워크 설정
variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "사용할 가용 영역들"
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
}

# EC2 설정
variable "backend_instance_type" {
  description = "백엔드 EC2 인스턴스 타입"
  type        = string
  default     = "t3.small"
}

variable "frontend_instance_type" {
  description = "프론트엔드 EC2 인스턴스 타입"
  type        = string
  default     = "t3.micro"
}

# RDS 설정
variable "db_instance_class" {
  description = "데이터베이스 인스턴스 타입"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "데이터베이스 이름"
  type        = string
  default     = "coffeeshout"
}

variable "db_username" {
  description = "데이터베이스 사용자 이름"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "db_password" {
  description = "데이터베이스 비밀번호"
  type        = string
  sensitive   = true
  # 실제 값은 terraform.tfvars 파일이나 환경변수로 설정
}
