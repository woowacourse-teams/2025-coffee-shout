output "backend_instance_id" {
  description = "백엔드 EC2 인스턴스 ID"
  value       = aws_instance.backend.id
}

output "backend_public_ip" {
  description = "백엔드 EC2 퍼블릭 IP"
  value       = aws_instance.backend.public_ip
}

output "backend_private_ip" {
  description = "백엔드 EC2 프라이빗 IP"
  value       = aws_instance.backend.private_ip
}

output "frontend_instance_id" {
  description = "프론트엔드 EC2 인스턴스 ID"
  value       = aws_instance.frontend.id
}

output "frontend_public_ip" {
  description = "프론트엔드 EC2 퍼블릭 IP"
  value       = aws_instance.frontend.public_ip
}

output "frontend_private_ip" {
  description = "프론트엔드 EC2 프라이빗 IP"
  value       = aws_instance.frontend.private_ip
}

output "backend_security_group_id" {
  description = "백엔드 보안 그룹 ID"
  value       = aws_security_group.backend.id
}

output "frontend_security_group_id" {
  description = "프론트엔드 보안 그룹 ID"
  value       = aws_security_group.frontend.id
}
