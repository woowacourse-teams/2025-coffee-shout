variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "key_name" {
  description = "EC2 key pair name for SSH access"
  type        = string
}

variable "data_volume_size" {
  description = "EBS volume size for monitoring data (GB)"
  type        = number
  default     = 30
}
