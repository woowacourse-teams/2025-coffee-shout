output "ec2_status_check_instance_alarm_arn" {
  description = "EC2 인스턴스 상태 체크 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.ec2_status_check_instance.arn
}

output "ec2_status_check_system_alarm_arn" {
  description = "EC2 시스템 상태 체크 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.ec2_status_check_system.arn
}

output "rds_cpu_high_alarm_arn" {
  description = "RDS CPU 높음 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.rds_cpu_high.arn
}

output "rds_storage_low_alarm_arn" {
  description = "RDS 스토리지 부족 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.rds_storage_low.arn
}

output "rds_connections_high_alarm_arn" {
  description = "RDS 연결 수 높음 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.rds_connections_high.arn
}

output "alb_unhealthy_host_alarm_arn" {
  description = "ALB Unhealthy 호스트 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.alb_unhealthy_host.arn
}

output "elasticache_cpu_high_alarm_arn" {
  description = "ElastiCache CPU 높음 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.elasticache_cpu_high.arn
}

output "elasticache_memory_high_alarm_arn" {
  description = "ElastiCache 메모리 높음 알람 ARN"
  value       = aws_cloudwatch_metric_alarm.elasticache_memory_high.arn
}
