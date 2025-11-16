# ========================================
# Application Load Balancer
# ========================================

resource "aws_lb" "main" {
  name               = "${var.project_name}-${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [var.security_group_id]
  subnets            = var.subnet_ids

  # 프리티어 최적화: 삭제 보호 비활성화
  enable_deletion_protection = false

  # 프리티어 최적화: 액세스 로그 비활성화
  # (S3 저장 비용 절감)
  # access_logs {
  #   bucket  = var.access_logs_bucket
  #   enabled = false
  # }

  # Cross-Zone 로드 밸런싱 (기본 활성화)
  enable_cross_zone_load_balancing = true

  # HTTP/2 활성화
  enable_http2 = true

  # Connection Draining
  idle_timeout = 310

  tags = merge(
    var.common_tags,
    {
      Name        = "${var.project_name}-${var.environment}-alb"
      Environment = var.environment
    }
  )
}

# ========================================
# Target Group (EC2 인스턴스용)
# ========================================

resource "aws_lb_target_group" "backend" {
  name     = "${var.project_name}-${var.environment}-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = var.vpc_id

  # 프리티어 최적화: Stickiness 비활성화 (세션 유지 불필요)
  stickiness {
    type    = "lb_cookie"
    enabled = false
  }

  # Health Check 설정 (Spring Boot Actuator)
  health_check {
    enabled             = true
    path                = var.health_check_path
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = var.health_check_interval
    timeout             = var.health_check_timeout
    healthy_threshold   = var.healthy_threshold
    unhealthy_threshold = var.unhealthy_threshold
    matcher             = "200"
  }

  # Connection Draining
  deregistration_delay = 310

  tags = merge(
    var.common_tags,
    {
      Name        = "${var.project_name}-${var.environment}-tg"
      Environment = var.environment
    }
  )
}

# ========================================
# Target Group Attachment (EC2 인스턴스 연결)
# ========================================

resource "aws_lb_target_group_attachment" "backend" {
  target_group_arn = aws_lb_target_group.backend.arn
  target_id        = var.target_instance_id
  port             = 8080
}

# ========================================
# HTTP Listener (포트 80)
# ========================================

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  # HTTPS가 활성화된 경우 리다이렉트, 아니면 Target Group으로 전달
  default_action {
    type = var.enable_https ? "redirect" : "forward"

    # HTTPS 리다이렉트 설정
    dynamic "redirect" {
      for_each = var.enable_https ? [1] : []
      content {
        port        = "443"
        protocol    = "HTTPS"
        status_code = "HTTP_301"
      }
    }

    # Target Group 전달 설정
    target_group_arn = var.enable_https ? null : aws_lb_target_group.backend.arn
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-http-listener"
    }
  )
}

# ========================================
# HTTPS Listener (포트 443, 선택적)
# ========================================

resource "aws_lb_listener" "https" {
  count = var.enable_https ? 1 : 0

  load_balancer_arn = aws_lb.main.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = var.certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-https-listener"
    }
  )
}
