variable "name" {
  description = "Name tag for EC2 instance"
  type        = string
}

variable "ami_id" {
  description = "AMI ID (leave empty to use latest Amazon Linux 2023 ARM64)"
  type        = string
  default     = ""
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t4g.small"
}

variable "subnet_id" {
  description = "Subnet ID"
  type        = string
}

variable "security_group_ids" {
  description = "List of security group IDs"
  type        = list(string)
}

variable "key_name" {
  description = "EC2 key pair name for SSH access"
  type        = string
  default     = null
}

variable "iam_instance_profile" {
  description = "IAM instance profile name"
  type        = string
  default     = null
}

variable "user_data" {
  description = "User data script"
  type        = string
  default     = null
}

variable "user_data_replace_on_change" {
  description = "Replace instance if user data changes"
  type        = bool
  default     = false
}

variable "root_volume_size" {
  description = "Root volume size in GB"
  type        = number
  default     = 20
}

variable "root_volume_type" {
  description = "Root volume type"
  type        = string
  default     = "gp3"
}

variable "tags" {
  description = "Additional tags"
  type        = map(string)
  default     = {}
}
