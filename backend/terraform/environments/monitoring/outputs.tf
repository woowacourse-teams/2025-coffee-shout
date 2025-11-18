output "vpc_id" {
  description = "VPC ID (from network state)"
  value       = local.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (from network state)"
  value       = local.public_subnet_ids
}

# EC2 Outputs
output "ec2_public_ip" {
  description = "Monitoring EC2 public IP (temporary, use elastic_ip instead)"
  value       = module.monitoring_ec2.public_ip
}

output "ec2_public_dns" {
  description = "Monitoring EC2 public DNS"
  value       = module.monitoring_ec2.public_dns
}

output "elastic_ip" {
  description = "Monitoring EC2 Elastic IP (fixed)"
  value       = aws_eip.monitoring.public_ip
}

# Security Group Output
output "security_group_id" {
  description = "Monitoring security group ID"
  value       = module.monitoring_sg.security_group_id
}

# EBS Volume Output
output "data_volume_id" {
  description = "Monitoring data volume ID"
  value       = aws_ebs_volume.monitoring_data.id
}

# Service URLs
output "grafana_url" {
  description = "Grafana URL"
  value       = "http://${aws_eip.monitoring.public_ip}:3000"
}

output "prometheus_url" {
  description = "Prometheus URL"
  value       = "http://${aws_eip.monitoring.public_ip}:9090"
}

output "loki_url" {
  description = "Loki URL"
  value       = "http://${aws_eip.monitoring.public_ip}:3100"
}

output "tempo_url" {
  description = "Tempo OTLP gRPC endpoint"
  value       = "${aws_eip.monitoring.public_ip}:4317"
}
