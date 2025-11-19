variable "name_prefix" {
  description = "Name prefix for ElastiCache resources"
  type        = string
}

variable "engine" {
  description = "Cache engine (redis, valkey, memcached)"
  type        = string
  default     = "valkey"
}

variable "engine_version" {
  description = "Engine version"
  type        = string
  default     = "7.2"
}

variable "node_type" {
  description = "Cache node type (use cache.t3.micro or cache.t4g.micro for free tier)"
  type        = string
  default     = "cache.t4g.micro"
}

variable "num_cache_nodes" {
  description = "Number of cache nodes (1 for free tier)"
  type        = number
  default     = 1
}

variable "port" {
  description = "Port number"
  type        = number
  default     = 6379
}

variable "subnet_ids" {
  description = "List of subnet IDs for cache subnet group"
  type        = list(string)
}

variable "security_group_ids" {
  description = "List of security group IDs"
  type        = list(string)
}

variable "parameter_group_family" {
  description = "Parameter group family"
  type        = string
  default     = "valkey7"
}

variable "create_parameter_group" {
  description = "Whether to create a parameter group"
  type        = bool
  default     = false
}

variable "parameter_group_name" {
  description = "Name of existing parameter group (if not creating new one)"
  type        = string
  default     = null
}

variable "parameters" {
  description = "List of parameters for the parameter group"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "maintenance_window" {
  description = "Preferred maintenance window (UTC)"
  type        = string
  default     = "sun:05:00-sun:06:00"
}

variable "snapshot_retention_limit" {
  description = "Number of days to retain snapshots (0 to disable)"
  type        = number
  default     = 0
}

variable "snapshot_window" {
  description = "Daily snapshot window (UTC)"
  type        = string
  default     = "03:00-05:00"
}

variable "notification_topic_arn" {
  description = "SNS topic ARN for notifications"
  type        = string
  default     = null
}

variable "tags" {
  description = "Additional tags"
  type        = map(string)
  default     = {}
}
