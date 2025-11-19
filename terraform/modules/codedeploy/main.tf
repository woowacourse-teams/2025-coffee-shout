# ========================================
# CodeDeploy Application (EC2 배포 무료)
# ========================================

resource "aws_codedeploy_app" "main" {
  name             = "${var.project_name}-${var.environment}-app"
  compute_platform = "Server" # EC2/온프레미스

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-app"
    }
  )
}

# CodeDeploy Deployment Group
resource "aws_codedeploy_deployment_group" "main" {
  app_name               = aws_codedeploy_app.main.name
  deployment_group_name  = "${var.project_name}-${var.environment}-deployment-group"
  service_role_arn       = var.codedeploy_role_arn
  deployment_config_name = var.deployment_config_name

  # EC2 인스턴스 타겟팅 (태그 기반)
  ec2_tag_set {
    ec2_tag_filter {
      key   = "Project"
      type  = "KEY_AND_VALUE"
      value = var.project_name
    }

    ec2_tag_filter {
      key   = "Environment"
      type  = "KEY_AND_VALUE"
      value = var.environment
    }
  }

  # Auto Rollback 설정
  auto_rollback_configuration {
    enabled = true
    events  = ["DEPLOYMENT_FAILURE", "DEPLOYMENT_STOP_ON_ALARM"]
  }

  # 로드밸런서 설정 (선택사항)
  # load_balancer_info {
  #   target_group_info {
  #     name = var.target_group_name
  #   }
  # }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-deployment-group"
    }
  )
}
