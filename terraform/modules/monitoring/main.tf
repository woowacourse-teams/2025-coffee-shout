# ========================================
# CloudWatch 알람 (AWS Free Tier: 10개까지 무료)
# ========================================
# 총 8개 알람 생성 (무료 한도 내)
# - EC2: 2개 (StatusCheckFailed_Instance, StatusCheckFailed_System)
# - RDS: 3개 (CPU, Storage, Connections)
# - ALB: 1개 (UnhealthyHostCount)
# - ElastiCache: 2개 (CPU, Memory)
# 알람 발생 시 SNS → Lambda → Slack 알림
# ========================================

# ========================================
# EC2 알람 (2개)
# ========================================

resource "aws_cloudwatch_metric_alarm" "ec2_status_check_instance" {
  alarm_name          = "${var.project_name}-${var.environment}-ec2-status-check-instance"
  alarm_description   = "EC2 인스턴스 상태 체크 실패 알람"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed_Instance"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = var.ec2_status_check_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    InstanceId = var.ec2_instance_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-ec2-status-check-instance"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "ec2_status_check_system" {
  alarm_name          = "${var.project_name}-${var.environment}-ec2-status-check-system"
  alarm_description   = "EC2 시스템 상태 체크 실패 알람"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed_System"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = var.ec2_status_check_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    InstanceId = var.ec2_instance_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-ec2-status-check-system"
    }
  )
}

# ========================================
# RDS 알람 (3개)
# ========================================

resource "aws_cloudwatch_metric_alarm" "rds_cpu_high" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-cpu-high"
  alarm_description   = "RDS CPU 사용률 높음 알람"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = var.rds_cpu_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    DBInstanceIdentifier = var.rds_instance_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-cpu-high"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_storage_low" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-storage-low"
  alarm_description   = "RDS 사용 가능한 스토리지 부족 알람"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = var.rds_storage_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    DBInstanceIdentifier = var.rds_instance_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-storage-low"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_connections_high" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-connections-high"
  alarm_description   = "RDS 데이터베이스 연결 수 높음 알람"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = var.rds_connections_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    DBInstanceIdentifier = var.rds_instance_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-connections-high"
    }
  )
}

# ========================================
# ALB 알람 (1개)
# ========================================

resource "aws_cloudwatch_metric_alarm" "alb_unhealthy_host" {
  alarm_name          = "${var.project_name}-${var.environment}-alb-unhealthy-host"
  alarm_description   = "ALB Unhealthy 호스트 감지 알람"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Maximum"
  threshold           = var.alb_unhealthy_host_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
    TargetGroup  = var.alb_target_group_arn_suffix
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-alb-unhealthy-host"
    }
  )
}

# ========================================
# ElastiCache 알람 (2개)
# ========================================

resource "aws_cloudwatch_metric_alarm" "elasticache_cpu_high" {
  alarm_name          = "${var.project_name}-${var.environment}-elasticache-cpu-high"
  alarm_description   = "ElastiCache CPU 사용률 높음 알람"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = var.elasticache_cpu_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    CacheClusterId = var.elasticache_cluster_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-elasticache-cpu-high"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "elasticache_memory_high" {
  alarm_name          = "${var.project_name}-${var.environment}-elasticache-memory-high"
  alarm_description   = "ElastiCache 메모리 사용률 높음 알람"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseMemoryUsagePercentage"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = var.elasticache_memory_threshold
  treat_missing_data  = "notBreaching"

  # SNS 알림
  alarm_actions = [var.sns_topic_arn]
  ok_actions    = [var.sns_topic_arn]

  dimensions = {
    CacheClusterId = var.elasticache_cluster_id
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-elasticache-memory-high"
    }
  )
}
