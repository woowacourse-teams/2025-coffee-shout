#!/bin/bash
export PATH="/usr/bin:/bin:$PATH"

echo "=== [BLOCK_TRAFFIC] 트래픽 차단 및 Graceful Shutdown ==="

cd /opt/coffee-shout

# ==========================================
# BlockTraffic 단계: 로드밸런서에서 트래픽 차단 시 실행됨
# 1. ALB가 타겟 등록 취소 (등록 취소 지연 시간 동안 기존 연결 유지)
# 2. 이 스크립트에서 SIGTERM으로 Graceful Shutdown 수행
# 3. ApplicationStop에서 남은 프로세스 강제 종료
# ==========================================

echo ""
echo "☕ 1. Spring Boot 애플리케이션 Graceful Shutdown 시작..."

if [ -f "app/coffee-shout.pid" ]; then
    PID=$(cat app/coffee-shout.pid)

    if ps -p $PID > /dev/null 2>&1; then
        echo "   🛑 SIGTERM 신호 전송 - Graceful Shutdown 시작 (PID: $PID)"
        kill -SIGTERM $PID

        # 최대 300초 (5분) 대기
        echo "   ⏳ Graceful Shutdown 대기 중... (최대 6분 - WebSocket 연결 종료 포함)"
        for i in {1..300}; do
            if ! ps -p $PID > /dev/null 2>&1; then
                echo "   ✅ Spring Boot 애플리케이션이 정상 종료되었습니다 (${i}초 소요)"
                break
            fi

            # 매 30초마다 진행 상황 출력
            if [ $((i % 30)) -eq 0 ]; then
                echo "   ⏱️  대기 중... (${i}초 경과)"
            fi

            sleep 1
        done

        # 여전히 실행 중이면 경고만 출력 (강제 종료는 ApplicationStop에서 수행)
        if ps -p $PID > /dev/null 2>&1; then
            echo "   ⚠️  Graceful Shutdown이 완료되지 않았습니다 (6분 초과)"
            echo "   ℹ️  ApplicationStop 단계에서 강제 종료를 진행합니다"
        fi
    else
        echo "   ℹ️  Spring Boot 애플리케이션이 이미 종료되어 있습니다"
        # PID 파일 제거
        rm -f app/coffee-shout.pid
    fi
else
    echo "   ℹ️  PID 파일이 없습니다. 애플리케이션이 실행 중이 아닐 수 있습니다"
fi

echo ""
echo "=== [BLOCK_TRAFFIC] 완료 ==="
