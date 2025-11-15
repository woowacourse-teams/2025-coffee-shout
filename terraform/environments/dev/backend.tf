terraform {
  backend "s3" {
    bucket         = "coffeeshout-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "coffeeshout-terraform-locks"
    encrypt        = true
  }
}
