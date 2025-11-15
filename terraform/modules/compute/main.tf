# 최신 Amazon Linux 2023 AMI 찾기
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# 백엔드 보안 그룹 (Spring Boot API)
resource "aws_security_group" "backend" {
  name        = "${var.project_name}-${var.environment}-backend-sg"
  description = "Security group for backend application"
  vpc_id      = var.vpc_id

  # HTTP 8080 포트 (Spring Boot 기본 포트)
  ingress {
    description = "Spring Boot API"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH 접속용
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # 실제 운영에서는 특정 IP로 제한하세요
  }

  # 모든 아웃바운드 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-backend-sg"
  }
}

# 프론트엔드 보안 그룹
resource "aws_security_group" "frontend" {
  name        = "${var.project_name}-${var.environment}-frontend-sg"
  description = "Security group for frontend application"
  vpc_id      = var.vpc_id

  # HTTP
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # 실제 운영에서는 특정 IP로 제한하세요
  }

  # 모든 아웃바운드 허용
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-frontend-sg"
  }
}

# 백엔드 EC2 인스턴스
resource "aws_instance" "backend" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = var.backend_instance_type
  subnet_id     = var.public_subnet_ids[0]

  vpc_security_group_ids = [aws_security_group.backend.id]

  user_data = <<-EOF
              #!/bin/bash
              # Java 17 설치 (Spring Boot용)
              sudo yum update -y
              sudo yum install -y java-17-amazon-corretto-devel

              # Docker 설치 (필요시)
              sudo yum install -y docker
              sudo systemctl start docker
              sudo systemctl enable docker
              sudo usermod -a -G docker ec2-user

              echo "Backend server setup complete" > /tmp/setup-complete.txt
              EOF

  tags = {
    Name = "${var.project_name}-${var.environment}-backend"
    Type = "Backend"
  }

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }
}

# 프론트엔드 EC2 인스턴스
resource "aws_instance" "frontend" {
  ami           = data.aws_ami.amazon_linux.id
  instance_type = var.frontend_instance_type
  subnet_id     = var.public_subnet_ids[0]

  vpc_security_group_ids = [aws_security_group.frontend.id]

  user_data = <<-EOF
              #!/bin/bash
              # Node.js 설치
              sudo yum update -y
              curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
              sudo yum install -y nodejs

              # Nginx 설치 (웹 서버)
              sudo yum install -y nginx
              sudo systemctl start nginx
              sudo systemctl enable nginx

              echo "Frontend server setup complete" > /tmp/setup-complete.txt
              EOF

  tags = {
    Name = "${var.project_name}-${var.environment}-frontend"
    Type = "Frontend"
  }

  root_block_device {
    volume_size = 10
    volume_type = "gp3"
  }
}
