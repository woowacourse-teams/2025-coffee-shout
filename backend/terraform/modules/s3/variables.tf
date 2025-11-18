variable "bucket_name" {
  description = "S3 bucket name"
  type        = string
}

variable "versioning_enabled" {
  description = "Enable versioning"
  type        = bool
  default     = false
}

variable "block_public_access" {
  description = "Block all public access"
  type        = bool
  default     = true
}

variable "cors_rules" {
  description = "CORS rules"
  type = list(object({
    allowed_headers = list(string)
    allowed_methods = list(string)
    allowed_origins = list(string)
    expose_headers  = optional(list(string))
    max_age_seconds = optional(number)
  }))
  default = []
}

variable "lifecycle_rules" {
  description = "Lifecycle rules"
  type = list(object({
    id     = string
    status = string
    prefix = optional(string)
    expiration = optional(object({
      days = number
    }))
    transitions = optional(list(object({
      days          = number
      storage_class = string
    })))
  }))
  default = []
}
