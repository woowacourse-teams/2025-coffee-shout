output "project_name" {
  description = "CodeBuild 프로젝트 이름"
  value       = aws_codebuild_project.main.name
}

output "project_arn" {
  description = "CodeBuild 프로젝트 ARN"
  value       = aws_codebuild_project.main.arn
}

output "project_id" {
  description = "CodeBuild 프로젝트 ID"
  value       = aws_codebuild_project.main.id
}
