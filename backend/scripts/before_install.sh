#!/bin/bash
set -e  # 에러 발생 시 즉시 종료

echo "=== [BEFORE_INSTALL] Docker 환경 준비 ==="

# 기존 컨테이너 정리
if command -v docker-compose &> /dev/null && [ -f "/opt/coffee-shout/compose.yaml" ]; then
    echo "기존 컨테이너를 정리합니다..."
    cd /opt/coffee-shout && docker-compose -f compose.yaml down || true
fi

# Docker 설치 확인 및 설치
if ! command -v docker &> /dev/null; then
    echo "Docker를 설치합니다..."
    yum update -y
    yum install -y docker
    systemctl start docker
    systemctl enable docker
    usermod -a -G docker ec2-user
else
    echo "Docker가 이미 설치되어 있습니다"
    systemctl start docker
fi

# Docker Compose 설치 확인 및 설치
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose를 설치합니다..."
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

echo "=== [BEFORE_INSTALL] 커피빵 게임 서버 배포 준비 ==="

# 기존 애플리케이션 안전하게 종료
if pgrep -f "coffee-shout" > /dev/null; then
    echo "기존 애플리케이션을 안전하게 종료합니다..."
    pkill -SIGTERM -f "coffee-shout" || true
    sleep 10

    # 강제 종료가 필요한 경우
    if pgrep -f "coffee-shout" > /dev/null; then
        echo "강제 종료를 진행합니다..."
        pkill -SIGKILL -f "coffee-shout" || true
    fi
fi

# 배포 디렉토리 생성 및 정리
sudo mkdir -p /opt/coffee-shout/{app,scripts,logs}
sudo chown -R ec2-user:ec2-user /opt/coffee-shout

echo "=== [BEFORE_INSTALL] 완료 ==="
