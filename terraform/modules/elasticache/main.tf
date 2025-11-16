# ========================================
# ElastiCache Subnet Group
# ========================================

resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-cache-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-cache-subnet-group"
    }
  )
}

# ========================================
# ElastiCache Parameter Group (Valkey)
# ========================================

resource "aws_elasticache_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-valkey${replace(var.engine_version, ".", "")}"
  family = "valkey${split(".", var.engine_version)[0]}"

  # 메모리 정책 (maxmemory-policy)
  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru" # LRU 방식으로 메모리 관리
  }

  # 타임아웃 설정
  parameter {
    name  = "timeout"
    value = "300"
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-valkey-params"
    }
  )
}

# ========================================
# ElastiCache Cluster (Valkey)
# ========================================

resource "aws_elasticache_cluster" "main" {
  cluster_id           = "${var.project_name}-${var.environment}-valkey"
  engine               = "valkey"
  engine_version       = var.engine_version
  node_type            = var.node_type
  num_cache_nodes      = 1 # Single node (프리티어)
  parameter_group_name = aws_elasticache_parameter_group.main.name
  port                 = 6379

  # 네트워크 설정
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [var.security_group_id]

  # 유지보수 윈도우
  maintenance_window = "mon:04:00-mon:05:00"

  # 자동 마이너 버전 업그레이드
  auto_minor_version_upgrade = true

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-valkey"
    }
  )
}
