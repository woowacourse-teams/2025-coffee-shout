#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

echo "=== [BEFORE_INSTALL] 커피빵 게임 서버 배포 준비 ==="

# 기존 애플리케이션 안전하게 종료
pid=$(pgrep -f "coffee-shout" || echo "")
if [ -n "$pid" ]; then
    echo "☕ 기존 애플리케이션을 안전하게 종료합니다..."
    pkill -SIGTERM -f "coffee-shout" || true
    sleep 10

    if kill -0 "$pid" 2>/dev/null; then
        echo "   🔨 강제 종료를 진행합니다..."
        pkill -SIGKILL -f "coffee-shout" || true
    fi
    echo "   ✅ 기존 애플리케이션 종료 완료"
else
    echo "☕ 실행 중인 애플리케이션이 없습니다"
fi

# 배포 디렉토리 생성 및 정리
echo "📁 배포 디렉토리 생성 및 권한 설정..."
sudo mkdir -p /opt/coffee-shout/{app,scripts,logs}
sudo chown -R ubuntu:ubuntu /opt/coffee-shout

# jq 설치 확인 및 설치
if ! command -v jq &> /dev/null; then
    echo "🔧 jq가 설치되어 있지 않습니다. 설치를 시작합니다..."
    if sudo yum install -y jq &>/dev/null 2>&1; then
        echo "✅ jq 설치 완료 (yum)"
    elif sudo apt-get install -y jq &>/dev/null 2>&1; then
        echo "✅ jq 설치 완료 (apt-get)"
    else
        echo "⚠️  jq 설치 실패. JSON 파싱 없이 계속 진행합니다."
    fi
else
    echo "✅ jq가 이미 설치되어 있습니다"
fi

# 기존 JAR 파일 삭제 (새 인스턴스 대응)
if [ -f "/opt/coffee-shout/app/coffee-shout-backend.jar" ]; then
    echo "🗑️  기존 JAR 파일 삭제..."
    sudo rm -f /opt/coffee-shout/app/coffee-shout-backend.jar
fi

echo "=== [BEFORE_INSTALL] 완료 ==="
