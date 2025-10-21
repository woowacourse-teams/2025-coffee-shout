#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

echo "=== [VALIDATE_SERVICE] 서비스 상태 검증 ==="

cd /opt/coffee-shout || {
    echo "❌ 디렉토리 이동 실패: /opt/coffee-shout"
    exit 1
}

# 헬스체크 (Spring Boot Actuator)
health_check() {
    local max_attempts=30
    local attempt=1

    while [ "$attempt" -le "$max_attempts" ]; do
        # Spring Boot Actuator 헬스체크 엔드포인트 확인
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost:8080/actuator/health 2>/dev/null || echo "000")

        if [ "$HTTP_CODE" = "200" ]; then
            echo "✅ 서버 헬스체크 성공 (시도: $attempt/$max_attempts)"

            # jq를 사용한 안전한 파싱
            if command -v jq &> /dev/null; then
                HEALTH_STATUS=$(curl -s --max-time 5 http://localhost:8080/actuator/health 2>/dev/null | jq -r '.status // "UNKNOWN"' 2>/dev/null)
                if [ "$HEALTH_STATUS" = "UP" ]; then
                    echo "✅ 애플리케이션 상태: UP"
                    return 0
                else
                    echo "⚠️  애플리케이션 상태: $HEALTH_STATUS (재시도...)"
                fi
            else
                # jq 미설치 시 HTTP 200만 확인
                if [ "$HTTP_CODE" = "200" ]; then
                    echo "✅ 애플리케이션 상태: HTTP 200 (jq 미설치)"
                    return 0
                fi
            fi
        elif [ "$HTTP_CODE" = "503" ]; then
            echo "⏳ 서버가 시작 중입니다 (HTTP 503)... (시도: $attempt/$max_attempts)"
        else
            echo "⏳ 서버 응답 대기 중 (HTTP $HTTP_CODE)... (시도: $attempt/$max_attempts)"
        fi

        sleep 2
        attempt=$((attempt + 1))
    done

    echo "❌ 서버 헬스체크 실패 (최대 시도 횟수 초과)"
    return 1
}

if health_check; then
    echo "🎉 커피빵 게임 서버 배포 완료!"
    echo ""
    echo "=== 서비스 정보 ==="
else
    echo "💥 헬스체크 실패!"
    exit 1
fi

echo "=== [VALIDATE_SERVICE] 완료 ==="
