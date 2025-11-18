output "dev_s3_bucket" {
  description = "Dev environment S3 bucket name"
  value       = aws_s3_bucket.terraform_state_dev.id
}

output "prod_s3_bucket" {
  description = "Prod environment S3 bucket name"
  value       = aws_s3_bucket.terraform_state_prod.id
}

output "instructions" {
  description = "Next steps"
  value       = <<-EOT
    Bootstrap 완료! 다음 단계:

    1. Dev 환경 초기화:
       cd ../environments/dev
       terraform init

    2. Prod 환경 초기화:
       cd ../environments/prod
       terraform init

    참고: Terraform 1.10+ S3 네이티브 locking 사용
    DynamoDB 테이블이 필요하지 않습니다.
  EOT
}
