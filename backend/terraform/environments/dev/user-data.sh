#!/bin/bash
set -e

# 시스템 업데이트
dnf update -y

# Java 21 설치
dnf install -y java-21-amazon-corretto

# Docker 설치
dnf install -y docker
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

# Docker Compose 설치
DOCKER_COMPOSE_VERSION="${docker_compose_version}"
curl -L "https://github.com/docker/compose/releases/download/v$${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

echo "Dev EC2 setup completed!" > /var/log/user-data.log
