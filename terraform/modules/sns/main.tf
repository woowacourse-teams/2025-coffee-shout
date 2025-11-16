# ========================================
# SNS Topic for CloudWatch Alarms
# ========================================
# CloudWatch 알람 → SNS → Lambda → Slack
# ========================================

resource "aws_sns_topic" "cloudwatch_alarms" {
  name         = "${var.project_name}-${var.environment}-cloudwatch-alarms"
  display_name = "CloudWatch Alarms for ${var.project_name} ${var.environment}"

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-cloudwatch-alarms"
    }
  )
}

# SNS Topic Policy (CloudWatch에서 Publish 허용)
resource "aws_sns_topic_policy" "cloudwatch_alarms" {
  arn = aws_sns_topic.cloudwatch_alarms.arn

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "cloudwatch.amazonaws.com"
        }
        Action   = "SNS:Publish"
        Resource = aws_sns_topic.cloudwatch_alarms.arn
      }
    ]
  })
}
