# ========================================
# Lambda Function for Slack Notifications
# ========================================
# CloudWatch 알람 → SNS → Lambda → Slack
# ========================================

# Lambda 함수 코드 ZIP 패키징
data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "${path.module}/src"
  output_path = "${path.module}/lambda_function.zip"
}

# Lambda IAM Role
resource "aws_iam_role" "lambda" {
  name = "${var.project_name}-${var.environment}-lambda-slack-notifier"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-lambda-slack-notifier"
    }
  )
}

# Lambda 기본 실행 권한 (CloudWatch Logs)
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Lambda 정책: SSM Parameter Store 읽기
resource "aws_iam_role_policy" "lambda_ssm" {
  name = "ssm-parameter-read"
  role = aws_iam_role.lambda.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters"
        ]
        Resource = var.ssm_parameter_arns
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = "*" # SSM SecureString은 기본 KMS 키 사용
      }
    ]
  })
}

# Lambda 함수
resource "aws_lambda_function" "slack_notifier" {
  filename         = data.archive_file.lambda_zip.output_path
  function_name    = "${var.project_name}-${var.environment}-slack-notifier"
  role             = aws_iam_role.lambda.arn
  handler          = "lambda_function.lambda_handler"
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
  runtime          = "python3.11"
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory_size

  environment {
    variables = {
      PROJECT_NAME = var.project_name
      ENVIRONMENT  = var.environment
    }
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-slack-notifier"
    }
  )
}

# SNS Topic 구독 (Lambda 트리거)
resource "aws_sns_topic_subscription" "lambda" {
  topic_arn = var.sns_topic_arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.slack_notifier.arn
}

# Lambda가 SNS에서 호출될 수 있도록 권한 부여
resource "aws_lambda_permission" "allow_sns" {
  statement_id  = "AllowExecutionFromSNS"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.slack_notifier.function_name
  principal     = "sns.amazonaws.com"
  source_arn    = var.sns_topic_arn
}

# CloudWatch Log Group (로그 보존 기간 설정)
resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/${aws_lambda_function.slack_notifier.function_name}"
  retention_in_days = 7 # 7일 후 자동 삭제

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-lambda-slack-notifier-logs"
    }
  )
}
