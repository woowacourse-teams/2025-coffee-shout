output "db_instance_id" {
  description = "RDS 인스턴스 ID"
  value       = aws_db_instance.main.id
}

output "db_endpoint" {
  description = "RDS 엔드포인트"
  value       = aws_db_instance.main.endpoint
  sensitive   = true
}

output "db_name" {
  description = "데이터베이스 이름"
  value       = aws_db_instance.main.db_name
}

output "db_port" {
  description = "데이터베이스 포트"
  value       = aws_db_instance.main.port
}

output "db_security_group_id" {
  description = "데이터베이스 보안 그룹 ID"
  value       = aws_security_group.database.id
}
