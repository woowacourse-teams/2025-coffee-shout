output "security_group_id" {
  description = "Security group ID"
  value       = aws_security_group.this.id
}

output "security_group_name" {
  description = "Security group name"
  value       = aws_security_group.this.name
}
