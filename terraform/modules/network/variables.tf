variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, staging, production)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
}

variable "public_subnet_cidrs" {
  description = "Public Subnet CIDR 블록 리스트"
  type        = list(string)
}

variable "private_subnet_cidrs" {
  description = "Private Subnet CIDR 블록 리스트"
  type        = list(string)
  default     = []
}

variable "availability_zones" {
  description = "사용할 가용 영역들"
  type        = list(string)
}

variable "enable_nat_gateway" {
  description = "NAT Gateway 생성 여부 (비용 절감을 위해 false 가능)"
  type        = bool
  default     = false
}

variable "common_tags" {
  description = "모든 리소스에 적용될 공통 태그"
  type        = map(string)
  default     = {}
}
