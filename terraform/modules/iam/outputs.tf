output "ec2_instance_profile_name" {
  description = "EC2 Instance Profile 이름"
  value       = aws_iam_instance_profile.ec2.name
}

output "ec2_instance_profile_arn" {
  description = "EC2 Instance Profile ARN"
  value       = aws_iam_instance_profile.ec2.arn
}

output "ec2_role_name" {
  description = "EC2 IAM Role 이름"
  value       = aws_iam_role.ec2_instance.name
}

output "ec2_role_arn" {
  description = "EC2 IAM Role ARN"
  value       = aws_iam_role.ec2_instance.arn
}

output "codedeploy_role_name" {
  description = "CodeDeploy Service Role 이름"
  value       = aws_iam_role.codedeploy.name
}

output "codedeploy_role_arn" {
  description = "CodeDeploy Service Role ARN"
  value       = aws_iam_role.codedeploy.arn
}

output "codebuild_role_name" {
  description = "CodeBuild Service Role 이름"
  value       = aws_iam_role.codebuild.name
}

output "codebuild_role_arn" {
  description = "CodeBuild Service Role ARN"
  value       = aws_iam_role.codebuild.arn
}
