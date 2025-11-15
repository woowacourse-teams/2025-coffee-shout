output "secret_id" {
  description = "Secrets Manager Secret ID"
  value       = aws_secretsmanager_secret.app_config.id
}

output "secret_arn" {
  description = "Secrets Manager Secret ARN"
  value       = aws_secretsmanager_secret.app_config.arn
}

output "secret_name" {
  description = "Secrets Manager Secret 이름"
  value       = aws_secretsmanager_secret.app_config.name
}

output "secret_version_id" {
  description = "Secrets Manager Secret Version ID"
  value       = aws_secretsmanager_secret_version.app_config.version_id
}
