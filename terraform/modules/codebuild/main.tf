# ========================================
# CodeBuild Project (무료 티어: 월 100분)
# ========================================

resource "aws_codebuild_project" "main" {
  name          = "${var.project_name}-${var.environment}-build"
  description   = "Build project for ${var.project_name} ${var.environment}"
  service_role  = var.codebuild_role_arn
  build_timeout = var.build_timeout

  artifacts {
    type = "CODEPIPELINE"
  }

  cache {
    type     = "S3"
    location = "${var.s3_bucket_name}/codebuild-cache"
  }

  environment {
    compute_type                = var.build_compute_type
    image                       = var.build_image
    type                        = "LINUX_CONTAINER"
    image_pull_credentials_type = "CODEBUILD"
    privileged_mode             = false

    environment_variable {
      name  = "ENVIRONMENT"
      value = var.environment
    }

    environment_variable {
      name  = "S3_BUCKET"
      value = var.s3_bucket_name
    }

    environment_variable {
      name  = "SNS_TOPIC_ARN"
      value = var.sns_topic_arn
    }

    # SSM Parameter Store에서 환경변수 자동 주입
    environment_variable {
      name  = "MYSQL_URL"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/mysql-url"
    }

    environment_variable {
      name  = "MYSQL_USERNAME"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/mysql-username"
    }

    environment_variable {
      name  = "MYSQL_PASSWORD"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/mysql-password"
    }

    environment_variable {
      name  = "S3_BUCKET_NAME"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/s3-bucket-name"
    }

    environment_variable {
      name  = "S3_QR_KEY_PREFIX"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/s3-qr-key-prefix"
    }

    environment_variable {
      name  = "REDIS_HOST"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/redis-host"
    }

    environment_variable {
      name  = "REDIS_PORT"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/redis-port"
    }

    environment_variable {
      name  = "TEMPO_URL"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/tempo-url"
    }

    environment_variable {
      name  = "TRACE_SAMPLING_PROBABILITY"
      type  = "PARAMETER_STORE"
      value = "/${var.project_name}/${var.environment}/trace-sampling-probability"
    }
  }

  logs_config {
    cloudwatch_logs {
      group_name  = "/aws/codebuild/${var.project_name}-${var.environment}"
      stream_name = "build"
    }
  }

  source {
    type      = "CODEPIPELINE"
    buildspec = "backend/buildspec-${var.environment}.yml"
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-build"
    }
  )
}

# CloudWatch Log Group (로그 보존 기간 설정)
resource "aws_cloudwatch_log_group" "codebuild" {
  name              = "/aws/codebuild/${var.project_name}-${var.environment}"
  retention_in_days = 7 # 7일 후 자동 삭제 (무료 티어 5GB 내)

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-codebuild-logs"
    }
  )
}
