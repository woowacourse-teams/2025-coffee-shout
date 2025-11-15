# Terraform이 리소스를 생성한 후 출력할 정보들

output "vpc_id" {
  description = "생성된 VPC ID"
  value       = module.network.vpc_id
}

output "public_subnet_ids" {
  description = "퍼블릭 서브넷 ID 목록"
  value       = module.network.public_subnet_ids
}

output "private_subnet_ids" {
  description = "프라이빗 서브넷 ID 목록"
  value       = module.network.private_subnet_ids
}

output "backend_instance_public_ip" {
  description = "백엔드 EC2 인스턴스 퍼블릭 IP"
  value       = module.compute.backend_public_ip
}

output "frontend_instance_public_ip" {
  description = "프론트엔드 EC2 인스턴스 퍼블릭 IP"
  value       = module.compute.frontend_public_ip
}

output "database_endpoint" {
  description = "RDS 데이터베이스 엔드포인트"
  value       = module.database.db_endpoint
  sensitive   = true
}

output "application_url" {
  description = "애플리케이션 접속 URL"
  value       = "http://${module.compute.frontend_public_ip}"
}
