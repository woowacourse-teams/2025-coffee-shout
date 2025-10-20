#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

echo "=== [BEFORE_INSTALL] 커피빵 게임 서버 배포 준비 ==="

# 기존 애플리케이션 안전하게 종료
if pgrep -f "coffee-shout" > /dev/null; then
    echo "☕ 기존 애플리케이션을 안전하게 종료합니다..."
    pkill -SIGTERM -f "coffee-shout" || true
    sleep 10

    # 강제 종료가 필요한 경우
    if pgrep -f "coffee-shout" > /dev/null; then
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

# 기존 JAR 파일 삭제 (새 인스턴스 대응)
if [ -f "/opt/coffee-shout/app/coffee-shout-backend.jar" ]; then
    echo "🗑️  기존 JAR 파일 삭제..."
    sudo rm -f /opt/coffee-shout/app/coffee-shout-backend.jar
fi

echo "=== [BEFORE_INSTALL] 완료 ==="
