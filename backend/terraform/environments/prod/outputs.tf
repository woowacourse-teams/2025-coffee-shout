output "vpc_id" {
  description = "VPC ID (from network state)"
  value       = local.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (from network state)"
  value       = local.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs (from network state)"
  value       = local.private_subnet_ids
}

# EC2 Outputs
output "ec2_public_ip" {
  description = "Prod EC2 public IP (temporary, use elastic_ip instead)"
  value       = module.prod_ec2.public_ip
}

output "ec2_public_dns" {
  description = "Prod EC2 public DNS"
  value       = module.prod_ec2.public_dns
}

output "elastic_ip" {
  description = "Prod EC2 Elastic IP (fixed)"
  value       = aws_eip.prod.public_ip
}

# Security Group Outputs
output "ec2_security_group_id" {
  description = "Prod EC2 security group ID"
  value       = module.prod_ec2_sg.security_group_id
}

output "rds_security_group_id" {
  description = "Prod RDS security group ID"
  value       = module.prod_rds_sg.security_group_id
}

output "elasticache_security_group_id" {
  description = "Prod ElastiCache security group ID"
  value       = module.prod_elasticache_sg.security_group_id
}

# RDS Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = module.prod_rds.db_instance_endpoint
}

output "rds_address" {
  description = "RDS instance address"
  value       = module.prod_rds.db_instance_address
}

output "rds_port" {
  description = "RDS instance port"
  value       = module.prod_rds.db_instance_port
}

output "db_name" {
  description = "Database name"
  value       = module.prod_rds.db_name
}

# ElastiCache Outputs
output "valkey_endpoint" {
  description = "Valkey cluster endpoint address"
  value       = module.prod_elasticache.cluster_address
}

output "valkey_port" {
  description = "Valkey cluster port"
  value       = module.prod_elasticache.port
}

# S3 Outputs
output "qr_codes_path" {
  description = "S3 path for prod QR codes"
  value       = "s3://${data.terraform_remote_state.network.outputs.s3_bucket_name}/qr/prod/"
}
