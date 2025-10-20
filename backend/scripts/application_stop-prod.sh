#!/bin/bash
export PATH="/usr/bin:/bin:$PATH"

echo "=== [APPLICATION_STOP] 강제 종료 확인 ==="

cd /opt/coffee-shout

# ==========================================
# ApplicationStop 단계: 강제 종료
# BeforeBlockTraffic 단계에서 SIGTERM 신호를 전송했고
# Spring Boot의 Graceful Shutdown이 진행되었어야 함
# 이 단계에서는 여전히 살아있는 프로세스를 강제로 종료
# ==========================================

echo ""
echo "☕ 1. Spring Boot 애플리케이션 종료 여부 확인..."

if [ -f "app/coffee-shout.pid" ]; then
    PID=$(cat app/coffee-shout.pid)

    if ps -p $PID > /dev/null 2>&1; then
        echo "   ⚠️  프로세스가 여전히 실행 중입니다 (PID: $PID)"
        echo "   🔨 강제 종료를 수행합니다 (SIGKILL)"
        kill -9 $PID 2>/dev/null || true
        sleep 2

        if ps -p $PID > /dev/null 2>&1; then
            echo "   ❌ 애플리케이션 강제 종료 실패"
            exit 1
        else
            echo "   ✅ 프로세스를 강제 종료했습니다"
        fi
    else
        echo "   ✅ Graceful Shutdown이 정상 완료되었습니다"
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
