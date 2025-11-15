output "bucket_id" {
  description = "S3 버킷 ID"
  value       = aws_s3_bucket.main.id
}

output "bucket_arn" {
  description = "S3 버킷 ARN"
  value       = aws_s3_bucket.main.arn
}

output "bucket_name" {
  description = "S3 버킷 이름"
  value       = aws_s3_bucket.main.bucket
}

output "bucket_regional_domain_name" {
  description = "S3 버킷 리전 도메인 이름"
  value       = aws_s3_bucket.main.bucket_regional_domain_name
}

# IAM 정책에서 사용할 경로별 ARN
output "codedeploy_artifacts_path_arn" {
  description = "CodeDeploy artifacts 경로 ARN"
  value       = "${aws_s3_bucket.main.arn}/codedeploy/artifacts/*"
}

output "qr_code_path_arn" {
  description = "QR 코드 경로 ARN"
  value       = "${aws_s3_bucket.main.arn}/qr-code/*"
}
