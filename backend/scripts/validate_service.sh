#!/bin/bash
set -e

echo "=== [VALIDATE_SERVICE] 서비스 상태 검증 ==="

cd /opt/coffee-shout

# 컨테이너 실행 확인
if ! docker-compose -f compose.yaml ps | grep -q "Up"; then
    echo "❌ 컨테이너가 실행되지 않습니다!"
    docker-compose -f compose.yaml logs
    exit 1
fi

echo "✅ 컨테이너가 실행 중입니다"

# 헬스체크 (Actuator 없는 환경 대응)
health_check() {
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        # 방법 1: 기본 루트 경로로 확인 (404여도 서버 응답하면 OK)
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ | grep -E "200|404" > /dev/null; then
            echo "✅ 서버 응답 확인됨 (시도: $attempt/$max_attempts)"
            return 0
        fi

        echo "⏳ 서버 응답 대기 중... (시도: $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    echo "❌ 서버 응답 없음!"
    return 1
}

if health_check; then
    echo "🎉 커피빵 게임 서버 배포 완료!"
    echo ""
    echo "=== 서비스 정보 ==="
    docker-compose -f compose.yaml ps
else
    echo "💥 헬스체크 실패!"
    docker-compose -f compose.yaml logs
    exit 1
fi

echo "=== [VALIDATE_SERVICE] 완료 ==="
