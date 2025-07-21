#!/bin/bash
set -e

echo "=== [APPLICATION_START] 커피빵 게임 서버 시작 ==="

cd /opt/coffee-shout

# ==========================================
# 1단계: MySQL 서버 시작 (Docker Compose)
# ==========================================
echo ""
echo "🗄️ 1. MySQL 서버 시작 중..."

if [ -f "compose.yaml" ]; then
    echo "   📄 compose.yaml 파일 확인됨"

    # Docker Compose로 MySQL 시작
    docker-compose -f compose.yaml up -d

    # MySQL 컨테이너 상태 확인
    echo "   📊 MySQL 컨테이너 상태 확인 중..."
    sleep 10
    docker-compose -f compose.yaml ps

    # MySQL 준비 대기
    echo "   ⏳ MySQL 서버 준비 대기 중..."
    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f compose.yaml exec -T mysql mysqladmin ping --silent 2>/dev/null; then
            echo "   ✅ MySQL 서버 준비 완료 (시도: $attempt/$max_attempts)"
            break
        fi

        echo "   ⏳ MySQL 준비 중... (시도: $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        echo "   ⚠️ MySQL 준비 시간 초과, 계속 진행합니다"
    fi

    echo "   ✅ MySQL 서버 시작 완료"
else
    echo "   ❌ compose.yaml 파일이 없습니다!"
    exit 1
fi

# ==========================================
# 2단계: Spring Boot JAR 애플리케이션 시작
# ==========================================
echo ""
echo "☕ 2. Spring Boot 애플리케이션 시작 중..."

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
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo "   🛑 기존 애플리케이션 프로세스 종료 중 (PID: $OLD_PID)"
        kill -SIGTERM $OLD_PID
        sleep 5

        # 강제 종료가 필요한 경우
        if ps -p $OLD_PID > /dev/null 2>&1; then
            kill -SIGKILL $OLD_PID
        fi
    fi
    rm -f app/coffee-shout.pid
fi

# JVM 옵션 설정
JVM_OPTS="-Xms512m -Xmx1024m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:+PrintGCDetails"
JVM_OPTS="$JVM_OPTS -Xloggc:logs/gc.log"
JVM_OPTS="$JVM_OPTS -Duser.timezone=Asia/Seoul"

# Spring Boot 애플리케이션 실행 (8080 포트)
echo "   🚀 Spring Boot 애플리케이션 시작 중..."
nohup java $JVM_OPTS \
    -jar app/coffee-shout-backend.jar --server.port=80 \
    > logs/application.log 2>&1 &

# PID 저장
echo $! > app/coffee-shout.pid
echo "   ✅ Spring Boot 애플리케이션 시작 완료 (PID: $(cat app/coffee-shout.pid))"
