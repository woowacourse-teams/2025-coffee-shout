variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod)"
  type        = string
}

variable "codedeploy_role_arn" {
  description = "CodeDeploy IAM Role ARN"
  type        = string
}

variable "ec2_instance_ids" {
  description = "배포 대상 EC2 인스턴스 ID 리스트"
  type        = list(string)
}

variable "deployment_config_name" {
  description = "배포 설정 (CodeDeployDefault.OneAtATime, AllAtOnce, HalfAtATime)"
  type        = string
  default     = "CodeDeployDefault.OneAtATime"
}

variable "common_tags" {
  description = "공통 태그"
  type        = map(string)
  default     = {}
}
