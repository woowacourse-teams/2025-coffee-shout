output "db_instance_id" {
  description = "RDS 인스턴스 ID"
  value       = aws_db_instance.main.id
}

output "db_instance_arn" {
  description = "RDS 인스턴스 ARN"
  value       = aws_db_instance.main.arn
}

output "db_endpoint" {
  description = "RDS 엔드포인트 (호스트:포트)"
  value       = aws_db_instance.main.endpoint
}

output "db_address" {
  description = "RDS 호스트 주소"
  value       = aws_db_instance.main.address
}

output "db_port" {
  description = "RDS 포트"
  value       = aws_db_instance.main.port
}

output "db_name" {
  description = "데이터베이스 이름"
  value       = aws_db_instance.main.db_name
}

output "db_username" {
  description = "데이터베이스 사용자 이름"
  value       = aws_db_instance.main.username
  sensitive   = true
}

output "db_password_secret_arn" {
  description = "데이터베이스 비밀번호가 저장된 Secrets Manager ARN"
  value       = aws_secretsmanager_secret.db_password.arn
}

output "db_password" {
  description = "데이터베이스 비밀번호 (민감정보)"
  value       = random_password.db_password.result
  sensitive   = true
}

# Spring Boot 연결 URL 형식
output "jdbc_url" {
  description = "JDBC 연결 URL"
  value       = "jdbc:mysql://${aws_db_instance.main.endpoint}/${aws_db_instance.main.db_name}?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
  sensitive   = true
}
