#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

echo "=== [BEFORE_INSTALL] 커피빵 게임 서버 배포 준비 ==="

# 기존 애플리케이션 안전하게 종료
# PID 파일이 있으면 우선 사용, 없으면 pgrep으로 찾기
if [ -f "/opt/coffee-shout/app/coffee-shout.pid" ]; then
    pid=$(cat /opt/coffee-shout/app/coffee-shout.pid 2>/dev/null || echo "")
else
    pid=$(pgrep -f "coffee-shout-backend.jar" || echo "")
fi

if [ -n "$pid" ] && ps -p "$pid" > /dev/null 2>&1; then
    echo "☕ 기존 애플리케이션을 안전하게 종료합니다... (PID: $pid)"
    kill -SIGTERM "$pid" 2>/dev/null || true

    # Graceful shutdown 대기 (최대 10초)
    for i in {1..10}; do
        if ! ps -p "$pid" > /dev/null 2>&1; then
            echo "   ✅ 기존 애플리케이션 종료 완료"
            break
        fi
        sleep 1
    done

    # 여전히 실행 중이면 강제 종료
    if ps -p "$pid" > /dev/null 2>&1; then
        echo "   🔨 강제 종료를 진행합니다..."
        kill -SIGKILL "$pid" 2>/dev/null || true
        sleep 2
        echo "   ✅ 기존 애플리케이션 강제 종료 완료"
    fi
else
    echo "☕ 실행 중인 애플리케이션이 없습니다"
fi

# PID 파일 정리
rm -f /opt/coffee-shout/app/coffee-shout.pid 2>/dev/null || true

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
