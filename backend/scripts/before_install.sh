#!/bin/bash
set -e  # 에러 발생 시 즉시 종료
export PATH="/usr/bin:/bin:$PATH"

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
sudo chown -R ubuntu:ubuntu /opt/coffee-shout

echo "=== [BEFORE_INSTALL] 완료 ==="
