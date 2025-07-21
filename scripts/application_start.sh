#!/bin/bash

echo "=== [APPLICATION_STOP] 애플리케이션 종료 ==="

cd /opt/coffee-bread

# Docker Compose로 서비스 시작
echo "compose.yaml을 사용하여 서비스를 시작합니다..."
docker-compose -f compose.yaml up -d

# 컨테이너 상태 확인
echo "컨테이너 상태 확인 중..."
sleep 10
docker-compose -f compose.yaml ps

if [ -f "app/coffee-shout.pid" ]; then
    PID=$(cat app/coffee-shout.pid)
    if ps -p $PID > /dev/null; then
        echo "애플리케이션을 안전하게 종료합니다 (PID: $PID)"
        kill -SIGTERM $PID

        # 최대 30초 대기
        for i in {1..30}; do
            if ! ps -p $PID > /dev/null; then
                echo "애플리케이션이 정상 종료되었습니다"
                break
            fi
            sleep 1
        done

        # 강제 종료가 필요한 경우
        if ps -p $PID > /dev/null; then
            echo "강제 종료를 진행합니다"
            kill -SIGKILL $PID
        fi
    fi
    rm -f app/coffee-shout.pid
fi

echo "=== [APPLICATION_STOP] 완료 ==="