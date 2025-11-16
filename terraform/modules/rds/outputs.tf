output "instance_id" {
  description = "RDS 인스턴스 ID"
  value       = aws_db_instance.main.id
}

output "instance_arn" {
  description = "RDS 인스턴스 ARN"
  value       = aws_db_instance.main.arn
}

output "endpoint" {
  description = "RDS 엔드포인트 (호스트:포트)"
  value       = aws_db_instance.main.endpoint
}

output "address" {
  description = "RDS 호스트 주소"
  value       = aws_db_instance.main.address
}

output "port" {
  description = "RDS 포트"
  value       = aws_db_instance.main.port
}

output "database_name" {
  description = "데이터베이스 이름"
  value       = aws_db_instance.main.db_name
}

output "username" {
  description = "데이터베이스 사용자 이름"
  value       = aws_db_instance.main.username
  sensitive   = true
}

output "password_secret_arn" {
  description = "데이터베이스 비밀번호가 저장된 Secrets Manager ARN"
  value       = aws_secretsmanager_secret.db_password.arn
}

output "password" {
  description = "데이터베이스 비밀번호 (민감정보)"
  value       = random_password.db_password.result
  sensitive   = true
}
