output "cluster_id" {
  description = "ElastiCache 클러스터 ID"
  value       = aws_elasticache_replication_group.main.id
}

output "cluster_arn" {
  description = "ElastiCache 클러스터 ARN"
  value       = aws_elasticache_replication_group.main.arn
}

output "cache_nodes" {
  description = "캐시 노드 정보"
  value       = aws_elasticache_replication_group.main.member_clusters
}

output "endpoint" {
  description = "ElastiCache 엔드포인트 (호스트:포트)"
  value       = "${aws_elasticache_replication_group.main.primary_endpoint_address}:${aws_elasticache_replication_group.main.port}"
}

output "host" {
  description = "ElastiCache 호스트 주소"
  value       = aws_elasticache_replication_group.main.primary_endpoint_address
}

output "port" {
  description = "ElastiCache 포트"
  value       = aws_elasticache_replication_group.main.port
}

output "configuration_endpoint" {
  description = "설정 엔드포인트"
  value       = aws_elasticache_replication_group.main.configuration_endpoint_address
}
