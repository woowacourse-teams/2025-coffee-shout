output "cluster_id" {
  description = "ElastiCache cluster/replication group ID"
  value       = var.engine == "valkey" ? aws_elasticache_replication_group.valkey[0].id : aws_elasticache_cluster.this[0].cluster_id
}

output "cluster_arn" {
  description = "ElastiCache cluster/replication group ARN"
  value       = var.engine == "valkey" ? aws_elasticache_replication_group.valkey[0].arn : aws_elasticache_cluster.this[0].arn
}

output "cache_nodes" {
  description = "List of cache nodes"
  value       = var.engine == "valkey" ? aws_elasticache_replication_group.valkey[0].member_clusters : aws_elasticache_cluster.this[0].cache_nodes
}

output "configuration_endpoint" {
  description = "Configuration endpoint (for Memcached)"
  value       = var.engine == "memcached" ? aws_elasticache_cluster.this[0].configuration_endpoint : null
}

output "cluster_address" {
  description = "Primary endpoint address"
  value       = var.engine == "valkey" ? aws_elasticache_replication_group.valkey[0].primary_endpoint_address : try(aws_elasticache_cluster.this[0].cache_nodes[0].address, null)
}

output "port" {
  description = "Port number"
  value       = var.engine == "valkey" ? aws_elasticache_replication_group.valkey[0].port : aws_elasticache_cluster.this[0].port
}

output "subnet_group_name" {
  description = "Cache subnet group name"
  value       = aws_elasticache_subnet_group.this.name
}
