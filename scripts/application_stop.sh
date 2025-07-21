#!/bin/bash

echo "=== [APPLICATION_STOP] 애플리케이션 종료 ==="

cd /opt/coffee-shout

if [ -f "compose.yaml" ]; then
    echo "compose.yaml 서비스를 종료합니다..."
    docker-compose -f compose.yaml down --timeout 30
    echo "서비스가 종료되었습니다"
else
    echo "compose.yaml 파일이 없습니다"
fi

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