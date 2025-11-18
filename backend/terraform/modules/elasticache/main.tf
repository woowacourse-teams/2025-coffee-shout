resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.name_prefix}-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(
    {
      Name = "${var.name_prefix}-subnet-group"
    },
    var.tags
  )
}

resource "aws_elasticache_parameter_group" "this" {
  count = var.create_parameter_group ? 1 : 0

  name   = "${var.name_prefix}-params"
  family = var.parameter_group_family

  dynamic "parameter" {
    for_each = var.parameters
    content {
      name  = parameter.value.name
      value = parameter.value.value
    }
  }

  tags = merge(
    {
      Name = "${var.name_prefix}-params"
    },
    var.tags
  )
}

# Valkey requires replication group
resource "aws_elasticache_replication_group" "valkey" {
  count = var.engine == "valkey" ? 1 : 0

  replication_group_id = var.name_prefix
  description          = "Valkey replication group for ${var.name_prefix}"

  engine         = "valkey"
  engine_version = var.engine_version

  # Node configuration
  node_type            = var.node_type
  num_cache_clusters   = var.num_cache_nodes
  port                 = var.port
  parameter_group_name = var.create_parameter_group ? aws_elasticache_parameter_group.this[0].name : var.parameter_group_name

  # Network configuration
  subnet_group_name  = aws_elasticache_subnet_group.this.name
  security_group_ids = var.security_group_ids

  # Maintenance and snapshot
  maintenance_window         = var.maintenance_window
  snapshot_retention_limit   = var.snapshot_retention_limit
  snapshot_window            = var.snapshot_window
  auto_minor_version_upgrade = true

  # Single node configuration (no automatic failover for free tier)
  automatic_failover_enabled = false
  multi_az_enabled           = false

  # Notifications
  notification_topic_arn = var.notification_topic_arn

  tags = merge(
    {
      Name = var.name_prefix
    },
    var.tags
  )
}

# Redis/Memcached use cluster
resource "aws_elasticache_cluster" "this" {
  count = var.engine != "valkey" ? 1 : 0

  cluster_id = var.name_prefix
  engine     = var.engine

  # Free tier eligible node type
  node_type       = var.node_type
  num_cache_nodes = var.num_cache_nodes
  port            = var.port

  # Parameter group
  parameter_group_name = var.create_parameter_group ? aws_elasticache_parameter_group.this[0].name : var.parameter_group_name

  # Network configuration
  subnet_group_name  = aws_elasticache_subnet_group.this.name
  security_group_ids = var.security_group_ids

  # Maintenance and snapshot
  maintenance_window       = var.maintenance_window
  snapshot_retention_limit = var.snapshot_retention_limit
  snapshot_window          = var.snapshot_window

  # Engine version
  engine_version = var.engine_version

  # Notifications (optional)
  notification_topic_arn = var.notification_topic_arn

  tags = merge(
    {
      Name = var.name_prefix
    },
    var.tags
  )
}
