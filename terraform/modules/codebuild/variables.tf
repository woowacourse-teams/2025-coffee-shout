variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "codebuild_role_arn" {
  description = "CodeBuild IAM Role ARN"
  type        = string
}

variable "s3_bucket_name" {
  description = "S3 버킷 이름 (캐시용)"
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

variable "build_compute_type" {
  description = "빌드 컴퓨팅 타입"
  type        = string
  default     = "BUILD_GENERAL1_SMALL" # 무료 티어
}

variable "build_image" {
  description = "빌드 이미지"
  type        = string
  default     = "aws/codebuild/standard:7.0" # Ubuntu 22.04, Java 17
}

variable "build_timeout" {
  description = "빌드 타임아웃 (분)"
  type        = number
  default     = 30
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}
