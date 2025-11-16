output "function_arn" {
  description = "Lambda 함수 ARN"
  value       = aws_lambda_function.slack_notifier.arn
}

output "function_name" {
  description = "Lambda 함수 이름"
  value       = aws_lambda_function.slack_notifier.function_name
}

output "function_invoke_arn" {
  description = "Lambda 함수 Invoke ARN"
  value       = aws_lambda_function.slack_notifier.invoke_arn
}

output "log_group_name" {
  description = "CloudWatch Log Group 이름"
  value       = aws_cloudwatch_log_group.lambda.name
}
