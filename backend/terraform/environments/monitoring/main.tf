terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket = "coffee-shout"
    key    = "terraform/tfstate/monitoring/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "coffee-shout"
      Environment = "monitoring"
      ManagedBy   = "Terraform"
    }
  }
}

# Network state에서 VPC 정보 가져오기
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "coffee-shout"
    key    = "terraform/tfstate/network/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

locals {
  vpc_id            = data.terraform_remote_state.network.outputs.vpc_id
  public_subnet_ids = data.terraform_remote_state.network.outputs.public_subnet_ids
}

# Security Group for Monitoring
module "monitoring_sg" {
  source = "../../modules/security-group"

  name_prefix = "coffee-shout-monitoring"
  description = "Security group for Monitoring EC2"
  vpc_id      = local.vpc_id

  ingress_rules = [
    {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "SSH"
    },
    {
      from_port   = 3000
      to_port     = 3000
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Grafana"
    },
    {
      from_port   = 9090
      to_port     = 9090
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Prometheus"
    },
    {
      from_port   = 3100
      to_port     = 3100
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Loki"
    },
    {
      from_port   = 4317
      to_port     = 4318
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Tempo (OTLP gRPC/HTTP)"
    }
  ]
}

# EC2 Instance
module "monitoring_ec2" {
  source = "../../modules/ec2"

  name               = "coffee-shout-monitoring"
  instance_type      = "t4g.small"
  subnet_id          = local.public_subnet_ids[0]
  security_group_ids = [module.monitoring_sg.security_group_id]
  key_name           = var.key_name

  user_data = file("${path.module}/user-data.sh")

  root_volume_size = 20
  root_volume_type = "gp3"

  tags = {
    Environment = "monitoring"
  }
}

# Elastic IP
resource "aws_eip" "monitoring" {
  instance = module.monitoring_ec2.instance_id
  domain   = "vpc"

  tags = {
    Name = "coffee-shout-monitoring-eip"
  }
}

# EBS Volume for monitoring data (Grafana, Prometheus, Loki, Tempo)
resource "aws_ebs_volume" "monitoring_data" {
  availability_zone = module.monitoring_ec2.availability_zone
  size              = var.data_volume_size
  type              = "gp3"
  encrypted         = true

  tags = {
    Name = "coffee-shout-monitoring-data"
  }
}

# Attach EBS Volume to EC2
resource "aws_volume_attachment" "monitoring_data" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.monitoring_data.id
  instance_id = module.monitoring_ec2.instance_id
}
