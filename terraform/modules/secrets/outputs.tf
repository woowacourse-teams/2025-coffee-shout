# SSM Parameter Store 파라미터 이름 (애플리케이션에서 참조용)
output "parameter_path_prefix" {
  description = "SSM Parameter Store 경로 프리픽스"
  value       = "/${var.project_name}/${var.environment}"
}

# 각 파라미터 이름
output "s3_bucket_name_parameter" {
  description = "S3 버킷 이름 파라미터 이름"
  value       = aws_ssm_parameter.s3_bucket_name.name
}

output "redis_host_parameter" {
  description = "Redis 호스트 파라미터 이름"
  value       = aws_ssm_parameter.redis_host.name
}

output "mysql_url_parameter" {
  description = "MySQL URL 파라미터 이름"
  value       = aws_ssm_parameter.mysql_url.name
}

output "mysql_username_parameter" {
  description = "MySQL 사용자명 파라미터 이름"
  value       = aws_ssm_parameter.mysql_username.name
}

output "mysql_password_parameter" {
  description = "MySQL 비밀번호 파라미터 이름"
  value       = aws_ssm_parameter.mysql_password.name
}

output "slack_bot_token_parameter" {
  description = "Slack Bot Token 파라미터 이름"
  value       = aws_ssm_parameter.slack_bot_token.name
}

output "slack_channel_parameter" {
  description = "Slack 채널 파라미터 이름"
  value       = aws_ssm_parameter.slack_channel.name
}

# Lambda에서 사용할 파라미터 ARN 목록
output "parameter_arns" {
  description = "모든 SSM 파라미터 ARN 목록 (IAM 권한용)"
  value = [
    aws_ssm_parameter.s3_bucket_name.arn,
    aws_ssm_parameter.s3_qr_key_prefix.arn,
    aws_ssm_parameter.environment.arn,
    aws_ssm_parameter.redis_host.arn,
    aws_ssm_parameter.redis_port.arn,
    aws_ssm_parameter.tempo_url.arn,
    aws_ssm_parameter.trace_sampling_probability.arn,
    aws_ssm_parameter.mysql_url.arn,
    aws_ssm_parameter.mysql_username.arn,
    aws_ssm_parameter.mysql_password.arn,
    aws_ssm_parameter.slack_bot_token.arn,
    aws_ssm_parameter.slack_channel.arn,
  ]
}
