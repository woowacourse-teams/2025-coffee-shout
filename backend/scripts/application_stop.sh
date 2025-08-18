#!/bin/bash
export PATH="/usr/bin:/bin:$PATH"

echo "=== [APPLICATION_STOP] 커피빵 게임 서버 종료 ==="

cd /opt/coffee-shout

# ==========================================
# 1단계: Spring Boot 애플리케이션 종료
# ==========================================
echo ""
echo "☕ 1. Spring Boot 애플리케이션 종료 중..."

if [ -f "app/coffee-shout.pid" ]; then
    PID=$(cat app/coffee-shout.pid)

    if ps -p $PID > /dev/null 2>&1; then
        echo "   🛑 Spring Boot 애플리케이션을 안전하게 종료합니다 (PID: $PID)"
        kill -SIGTERM $PID

        # 최대 30초 대기
        echo "   ⏳ 정상 종료 대기 중..."
        for i in {1..30}; do
            if ! ps -p $PID > /dev/null 2>&1; then
                echo "   ✅ Spring Boot 애플리케이션이 정상 종료되었습니다"
                break
            fi
            sleep 1
        done

        # 강제 종료가 필요한 경우
        if ps -p $PID > /dev/null 2>&1; then
            echo "   ⚠️ 강제 종료를 진행합니다"
            kill -SIGKILL $PID
            sleep 2

            if ps -p $PID > /dev/null 2>&1; then
                echo "   ❌ 애플리케이션 종료 실패"
            else
                echo "   ✅ 애플리케이션 강제 종료 완료"
            fi
        fi
    else
        echo "   ℹ️ Spring Boot 애플리케이션이 이미 종료되어 있습니다"
    fi

    # PID 파일 제거
    rm -f app/coffee-shout.pid
else
    echo "   ℹ️ PID 파일이 없습니다. 애플리케이션이 실행 중이 아닐 수 있습니다"
fi

# 포트 8080 사용 프로세스 강제 종료 (혹시 모를 좀비 프로세스)
JAVA_PROCESS=$(lsof -ti:8080 2>/dev/null || true)
if [ ! -z "$JAVA_PROCESS" ]; then
    echo "   🔫 포트 8080을 사용하는 프로세스 강제 종료 (PID: $JAVA_PROCESS)"
    kill -9 $JAVA_PROCESS 2>/dev/null || true
fi
