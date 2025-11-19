variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "s3_bucket_name" {
  description = "S3 버킷 이름 (아티팩트 저장용)"
  type        = string
}

variable "github_connection_arn" {
  description = "GitHub CodeStar Connection ARN (미리 생성 필요)"
  type        = string
}

variable "github_repo" {
  description = "GitHub 리포지토리 (예: owner/repo)"
  type        = string
}

variable "github_branch" {
  description = "GitHub 브랜치"
  type        = string
  default     = "main"
}

variable "codebuild_project_name" {
  description = "CodeBuild 프로젝트 이름"
  type        = string
}

variable "codedeploy_app_name" {
  description = "CodeDeploy 애플리케이션 이름"
  type        = string
}

variable "codedeploy_deployment_group_name" {
  description = "CodeDeploy 배포 그룹 이름"
  type        = string
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}
