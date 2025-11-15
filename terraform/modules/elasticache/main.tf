# ElastiCache Subnet Group (Private Subnet에 배치)
resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-cache-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-cache-subnet-group"
    }
  )
}

# ElastiCache Parameter Group (Valkey 설정)
resource "aws_elasticache_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-valkey7"
  family = "valkey7"

  # 메모리 정책 (maxmemory-policy)
  parameter {
    name  = "maxmemory-policy"
    value = "allkeys-lru"  # LRU 방식으로 메모리 관리
  }

  # 타임아웃 설정
  parameter {
    name  = "timeout"
    value = "300"
  }

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-valkey7-params"
    }
  )
}

# ElastiCache Cluster (Valkey)
resource "aws_elasticache_cluster" "main" {
  cluster_id           = "${var.project_name}-${var.environment}-valkey"
  engine               = "valkey"
  engine_version       = "7.2"
  node_type            = var.cache_node_type
  num_cache_nodes      = 1  # Single node (프리티어)
  parameter_group_name = aws_elasticache_parameter_group.main.name
  port                 = 6379

  # 네트워크 설정
  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [var.cache_security_group_id]

  # 스냅샷 설정
  snapshot_retention_limit = var.snapshot_retention_limit
  snapshot_window          = "03:00-04:00"  # 새벽 3-4시 (한국 시간 기준)

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
