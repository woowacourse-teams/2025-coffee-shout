# RDS CloudWatch Log Groups (생명 주기 정책 적용)
resource "aws_cloudwatch_log_group" "rds_error" {
  name              = "/aws/rds/instance/${var.project_name}-${var.environment}-mysql/error"
  retention_in_days = var.log_retention_days

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-error-logs"
    }
  )
}

resource "aws_cloudwatch_log_group" "rds_general" {
  name              = "/aws/rds/instance/${var.project_name}-${var.environment}-mysql/general"
  retention_in_days = var.log_retention_days

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-general-logs"
    }
  )
}

resource "aws_cloudwatch_log_group" "rds_slowquery" {
  name              = "/aws/rds/instance/${var.project_name}-${var.environment}-mysql/slowquery"
  retention_in_days = var.log_retention_days

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-slowquery-logs"
    }
  )
}
