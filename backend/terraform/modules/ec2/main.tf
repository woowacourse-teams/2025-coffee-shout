data "aws_ami" "amazon_linux_2023_arm64" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-arm64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "this" {
  ami           = var.ami_id != "" ? var.ami_id : data.aws_ami.amazon_linux_2023_arm64.id
  instance_type = var.instance_type
  subnet_id     = var.subnet_id
  key_name      = var.key_name

  vpc_security_group_ids = var.security_group_ids
  iam_instance_profile   = var.iam_instance_profile

  user_data                   = var.user_data
  user_data_replace_on_change = var.user_data_replace_on_change

  root_block_device {
    volume_size           = var.root_volume_size
    volume_type           = var.root_volume_type
    delete_on_termination = true
    encrypted             = true
  }

  tags = merge(
    {
      Name = var.name
    },
    var.tags
  )
}
