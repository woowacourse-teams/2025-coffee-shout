variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "key_name" {
  description = "EC2 key pair name for SSH access"
  type        = string
}

# RDS variables
variable "db_name" {
  description = "Database name"
  type        = string
  default     = "coffeeshout"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "admin"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}
