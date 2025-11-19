output "instance_id" {
  description = "EC2 인스턴스 ID"
  value       = aws_instance.backend.id
}

output "instance_arn" {
  description = "EC2 인스턴스 ARN"
  value       = aws_instance.backend.arn
}

output "private_ip" {
  description = "EC2 프라이빗 IP"
  value       = aws_instance.backend.private_ip
}

output "public_ip" {
  description = "EC2 퍼블릭 IP"
  value       = aws_instance.backend.public_ip
}

output "eip" {
  description = "Elastic IP (할당된 경우)"
  value       = var.assign_eip ? aws_eip.backend[0].public_ip : null
}

output "availability_zone" {
  description = "가용 영역"
  value       = aws_instance.backend.availability_zone
}
