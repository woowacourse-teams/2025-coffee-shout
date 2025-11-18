terraform {
  backend "s3" {
    bucket         = "coffeeshout-terraform-state-dev"
    key            = "terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    use_lockfile   = true # S3 네이티브 state locking (Terraform 1.10+)
  }
}
