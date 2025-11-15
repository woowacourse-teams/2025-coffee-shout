# ========================================
# Network Outputs
# ========================================

output "vpc_id" {
  description = "VPC ID"
  value       = module.network.vpc_id
}

output "public_subnet_ids" {
  description = "Public Subnet IDs"
  value       = module.network.public_subnet_ids
}

# ========================================
# EC2 Outputs
# ========================================

output "ec2_instance_id" {
  description = "EC2 인스턴스 ID"
  value       = module.ec2.instance_id
}

output "ec2_private_ip" {
  description = "EC2 프라이빗 IP"
  value       = module.ec2.private_ip
}

output "ec2_public_ip" {
  description = "EC2 퍼블릭 IP"
  value       = module.ec2.public_ip
}

# ========================================
# ALB Outputs
# ========================================

output "alb_dns_name" {
  description = "ALB DNS 이름"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "ALB Route53 Zone ID"
  value       = module.alb.alb_zone_id
}

# ========================================
# S3 Outputs
# ========================================

output "s3_bucket_name" {
  description = "S3 버킷 이름"
  value       = module.s3.bucket_name
}

output "s3_bucket_arn" {
  description = "S3 버킷 ARN"
  value       = module.s3.bucket_arn
}

# ========================================
# ElastiCache Outputs (DEV)
# ========================================

output "elasticache_endpoint" {
  description = "ElastiCache 엔드포인트"
  value       = module.elasticache.endpoint
}

output "elasticache_address" {
  description = "ElastiCache 주소"
  value       = module.elasticache.address
}

output "elasticache_port" {
  description = "ElastiCache 포트"
  value       = module.elasticache.port
}

# ========================================
# Secrets Manager Outputs
# ========================================

output "secrets_manager_name" {
  description = "Secrets Manager Secret 이름"
  value       = module.secrets.secret_name
}

output "secrets_manager_arn" {
  description = "Secrets Manager Secret ARN"
  value       = module.secrets.secret_arn
}

# ========================================
# Docker MySQL 정보 (DEV 전용)
# ========================================

output "docker_mysql_host" {
  description = "Docker MySQL 호스트 (localhost)"
  value       = "localhost:3306"
}

output "docker_compose_location" {
  description = "Docker Compose 파일 위치"
  value       = "/opt/coffee-shout/docker-compose.yml"
}
