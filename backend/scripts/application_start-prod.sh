#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

echo "=== [APPLICATION_START] 커피빵 게임 서버 시작 ==="

cd /opt/coffee-shout || {
    echo "❌ 디렉토리 이동 실패: /opt/coffee-shout"
    exit 1
}

# ==========================================
# 1단계: Spring Boot JAR 애플리케이션 시작
# ==========================================
echo "☕ 1. Spring Boot 애플리케이션 시작 중..."

# JAR 파일 확인
if [ -f "app/coffee-shout-backend.jar" ]; then
    echo "   📄 JAR 파일 확인됨: coffee-shout-backend.jar"
else
    echo "   ❌ JAR 파일을 찾을 수 없습니다!"
    exit 1
fi

# 기존 JAR 프로세스 종료 (있다면)
if [ -f "app/coffee-shout.pid" ]; then
    OLD_PID=$(cat app/coffee-shout.pid)
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "   🛑 기존 애플리케이션 프로세스 종료 중 (PID: $OLD_PID)"
        kill -SIGTERM "$OLD_PID"
        sleep 5

        # 강제 종료가 필요한 경우
        if ps -p "$OLD_PID" > /dev/null 2>&1; then
            kill -SIGKILL "$OLD_PID"
        fi
    fi
    rm -f app/coffee-shout.pid
fi

# JVM 옵션 설정
JVM_OPTS=(
  -Xms256m
  -Xmx512m
  -XX:+UseParallelGC
  -XX:+PrintGCDetails
  -Xloggc:logs/gc.log
  -Duser.timezone=Asia/Seoul
)

# 환경 프로파일 설정
SPRING_PROFILE="prod"
export SPRING_PROFILES_ACTIVE="$SPRING_PROFILE"
echo "   🌍 환경 프로파일: $SPRING_PROFILE"

# Spring Boot 애플리케이션 실행 (8080 포트)
echo "   🚀 Spring Boot 애플리케이션 시작 중..."
nohup java "${JVM_OPTS[@]}" \
    -Dspring.profiles.active="$SPRING_PROFILE" \
    -jar app/coffee-shout-backend.jar \
    > logs/application.log 2>&1 &

# PID 저장
APP_PID=$!
echo "$APP_PID" > app/coffee-shout.pid
echo "   ✅ Spring Boot 애플리케이션 시작 완료 (PID: $APP_PID)"
