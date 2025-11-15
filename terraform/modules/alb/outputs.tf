output "alb_id" {
  description = "ALB ID"
  value       = aws_lb.main.id
}

output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

output "alb_dns_name" {
  description = "ALB DNS 이름"
  value       = aws_lb.main.dns_name
}

output "alb_zone_id" {
  description = "ALB Route53 Zone ID"
  value       = aws_lb.main.zone_id
}

output "target_group_id" {
  description = "Target Group ID"
  value       = aws_lb_target_group.backend.id
}

output "target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.backend.arn
}

output "http_listener_arn" {
  description = "HTTP Listener ARN"
  value       = aws_lb_listener.http.arn
}

output "https_listener_arn" {
  description = "HTTPS Listener ARN (활성화된 경우)"
  value       = var.enable_https ? aws_lb_listener.https[0].arn : null
}
