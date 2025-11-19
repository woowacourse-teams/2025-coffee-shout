output "vpc_id" {
  description = "VPC ID (from network state)"
  value       = local.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (from network state)"
  value       = local.public_subnet_ids
}

output "ec2_public_ip" {
  description = "Dev EC2 public IP (temporary, use elastic_ip instead)"
  value       = module.dev_ec2.public_ip
}

output "ec2_public_dns" {
  description = "Dev EC2 public DNS"
  value       = module.dev_ec2.public_dns
}

output "elastic_ip" {
  description = "Dev EC2 Elastic IP (fixed)"
  value       = aws_eip.dev.public_ip
}

output "security_group_id" {
  description = "Dev security group ID"
  value       = module.dev_sg.security_group_id
}

output "qr_codes_path" {
  description = "S3 path for dev QR codes"
  value       = "s3://${data.terraform_remote_state.network.outputs.s3_bucket_name}/qr/dev/"
}
