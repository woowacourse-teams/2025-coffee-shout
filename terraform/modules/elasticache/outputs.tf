output "cluster_id" {
  description = "ElastiCache 클러스터 ID"
  value       = aws_elasticache_cluster.main.cluster_id
}

output "cluster_arn" {
  description = "ElastiCache 클러스터 ARN"
  value       = aws_elasticache_cluster.main.arn
}

output "cache_nodes" {
  description = "캐시 노드 정보"
  value       = aws_elasticache_cluster.main.cache_nodes
}

output "endpoint" {
  description = "ElastiCache 엔드포인트 (호스트:포트)"
  value       = "${aws_elasticache_cluster.main.cache_nodes[0].address}:${aws_elasticache_cluster.main.cache_nodes[0].port}"
}

output "host" {
  description = "ElastiCache 호스트 주소"
  value       = aws_elasticache_cluster.main.cache_nodes[0].address
}

output "port" {
  description = "ElastiCache 포트"
  value       = aws_elasticache_cluster.main.cache_nodes[0].port
}

output "configuration_endpoint" {
  description = "설정 엔드포인트"
  value       = aws_elasticache_cluster.main.configuration_endpoint
}
