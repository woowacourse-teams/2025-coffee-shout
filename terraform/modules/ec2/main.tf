# 최신 Ubuntu 24.04 ARM64 AMI 찾기
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-arm64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# User Data 스크립트 (초기 설정)
locals {
  user_data = <<-EOF
    #!/bin/bash
    set -e

    # 로그 파일
    exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

    echo "Starting user data script..."

    # 시스템 업데이트
    apt-get update -y

    # Java 21 설치 (Amazon Corretto)
    wget -O- https://apt.corretto.aws/corretto.key | apt-key add -
    add-apt-repository 'deb https://apt.corretto.aws stable main'
    apt-get update -y
    apt-get install -y java-21-amazon-corretto-jdk

    # Docker 설치
    apt-get install -y ca-certificates curl
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    # Docker 서비스 시작 및 자동 시작 설정
    systemctl start docker
    systemctl enable docker

    # ubuntu 사용자를 docker 그룹에 추가
    usermod -aG docker ubuntu

    # CodeDeploy Agent 설치
    apt-get install -y ruby-full wget
    cd /home/ubuntu
    wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
    chmod +x ./install
    ./install auto

    # CodeDeploy Agent 시작 및 자동 시작 설정
    systemctl start codedeploy-agent
    systemctl enable codedeploy-agent

    # AWS CLI 설치 (Secrets Manager 접근용)
    apt-get install -y awscli

    # Secrets Manager에서 환경 변수 가져오는 헬퍼 스크립트 생성
    cat > /usr/local/bin/load-secrets.sh <<'SCRIPT'
    #!/bin/bash
    # Secrets Manager에서 환경 변수를 가져와 파일로 저장
    SECRET_NAME="${secret_name}"
    REGION="ap-northeast-2"

    # Secrets Manager에서 JSON 가져오기
    aws secretsmanager get-secret-value \
      --secret-id "$SECRET_NAME" \
      --region "$REGION" \
      --query SecretString \
      --output text > /tmp/secrets.json

    # JSON을 환경 변수 형식으로 변환하여 저장
    mkdir -p /etc/environment.d
    cat /tmp/secrets.json | jq -r 'to_entries|map("\(.key)=\(.value|tostring)")|.[]' > /etc/environment.d/app-secrets.conf

    # 환경 변수 파일을 /opt/coffee-shout/.env에도 복사 (애플리케이션용)
    cp /etc/environment.d/app-secrets.conf /opt/coffee-shout/.env

    rm /tmp/secrets.json
    echo "Secrets loaded successfully!"
    SCRIPT

    chmod +x /usr/local/bin/load-secrets.sh

    # 애플리케이션 디렉토리 생성 (be/prod 구조와 동일)
    mkdir -p /opt/coffee-shout/app /opt/coffee-shout/scripts /opt/coffee-shout/logs
    chown -R ubuntu:ubuntu /opt/coffee-shout

    # 부팅 시 자동으로 Secrets 로드하도록 systemd 서비스 생성
    cat > /etc/systemd/system/load-secrets.service <<'SERVICE'
    [Unit]
    Description=Load secrets from AWS Secrets Manager
    After=network.target

    [Service]
    Type=oneshot
    ExecStart=/usr/local/bin/load-secrets.sh
    RemainAfterExit=yes

    [Install]
    WantedBy=multi-user.target
    SERVICE

    systemctl daemon-reload
    systemctl enable load-secrets.service
    systemctl start load-secrets.service

    echo "User data script completed successfully!"
    EOF
}

# EC2 인스턴스 (Backend)
resource "aws_instance" "backend" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = var.instance_type
  subnet_id     = var.subnet_id

  # IAM Instance Profile (Secrets Manager, S3, CodeDeploy 접근)
  iam_instance_profile = var.iam_instance_profile_name

  # Security Group
  vpc_security_group_ids = [var.security_group_id]

  # User Data
  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    secret_name = var.secrets_manager_name
    environment = var.environment
  }))

  # EBS 볼륨
  root_block_device {
    volume_size = var.root_volume_size
    volume_type = "gp3"
    encrypted   = true

    tags = merge(
      var.common_tags,
      {
        Name = "${var.project_name}-${var.environment}-${var.instance_name}-root"
      }
    )
  }

  # 메타데이터 옵션 (IMDSv2 강제)
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  tags = merge(
    var.common_tags,
    {
      Name        = "${var.project_name}-${var.environment}-${var.instance_name}"
      Environment = var.environment
      Role        = "Backend"
    }
  )

  lifecycle {
    ignore_changes = [
      ami, # AMI 업데이트 시 재생성 방지
      user_data
    ]
  }
}

# Elastic IP (선택적)
resource "aws_eip" "backend" {
  count    = var.assign_eip ? 1 : 0
  instance = aws_instance.backend.id
  domain   = "vpc"

  tags = merge(
    var.common_tags,
    {
      Name = "${var.project_name}-${var.environment}-${var.instance_name}-eip"
    }
  )
}
