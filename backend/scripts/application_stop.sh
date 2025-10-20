#!/bin/bash
export PATH="/usr/bin:/bin:$PATH"

echo "=== [APPLICATION_STOP] 커피빵 게임 서버 강제 종료 ==="

cd /opt/coffee-shout

# ==========================================
# ApplicationStop 단계: 강제 종료
# Graceful Shutdown은 BlockTraffic 단계에서 이미 시도됨
# 이 단계에서는 남아있는 프로세스를 강제로 정리
# ==========================================

echo ""
echo "☕ 1. Spring Boot 애플리케이션 강제 종료 중..."

if [ -f "app/coffee-shout.pid" ]; then
    PID=$(cat app/coffee-shout.pid)

    if ps -p $PID > /dev/null 2>&1; then
        echo "   ⚠️  Graceful Shutdown이 완료되지 않은 프로세스 발견 (PID: $PID)"
        echo "   🔫 SIGKILL 신호 전송 - 강제 종료 진행"
        kill -SIGKILL $PID 2>/dev/null || true
        sleep 2

        if ps -p $PID > /dev/null 2>&1; then
            echo "   ❌ 애플리케이션 강제 종료 실패"
            exit 1
        else
            echo "   ✅ 애플리케이션 강제 종료 완료"
        fi
    else
        echo "   ✅ Spring Boot 애플리케이션이 이미 종료되어 있습니다"
    fi

    # PID 파일 제거
    rm -f app/coffee-shout.pid
else
    echo "   ℹ️  PID 파일이 없습니다"
fi

# 포트 8080 사용 프로세스 강제 종료 (혹시 모를 좀비 프로세스)
JAVA_PROCESS=$(lsof -ti:8080 2>/dev/null || true)
if [ ! -z "$JAVA_PROCESS" ]; then
    echo "   🔫 포트 8080을 사용하는 좀비 프로세스 강제 종료 (PID: $JAVA_PROCESS)"
    kill -9 $JAVA_PROCESS 2>/dev/null || true
    sleep 1
fi

echo ""
echo "=== [APPLICATION_STOP] 완료 ==="
