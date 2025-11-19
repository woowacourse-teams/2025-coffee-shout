output "app_name" {
  description = "CodeDeploy 애플리케이션 이름"
  value       = aws_codedeploy_app.main.name
}

output "app_id" {
  description = "CodeDeploy 애플리케이션 ID"
  value       = aws_codedeploy_app.main.id
}

output "deployment_group_name" {
  description = "CodeDeploy 배포 그룹 이름"
  value       = aws_codedeploy_deployment_group.main.deployment_group_name
}

output "deployment_group_id" {
  description = "CodeDeploy 배포 그룹 ID"
  value       = aws_codedeploy_deployment_group.main.id
}
