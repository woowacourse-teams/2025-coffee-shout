output "pipeline_name" {
  description = "CodePipeline 이름"
  value       = aws_codepipeline.main.name
}

output "pipeline_id" {
  description = "CodePipeline ID"
  value       = aws_codepipeline.main.id
}

output "pipeline_arn" {
  description = "CodePipeline ARN"
  value       = aws_codepipeline.main.arn
}
