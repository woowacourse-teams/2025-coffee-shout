variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "vpc_cidr" {
  description = "VPC CIDR block (Dev + Prod 공용)"
  type        = string
  default     = "10.0.0.0/16"
}
