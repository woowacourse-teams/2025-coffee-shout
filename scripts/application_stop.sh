#!/bin/bash

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

# ==========================================
# 2단계: MySQL 서버 종료 (Docker Compose)
# ==========================================
echo ""
echo "🗄️ 2. MySQL 서버 종료 중..."

if [ -f "compose.yaml" ]; then
    echo "   📄 compose.yaml을 사용하여 MySQL 서버를 종료합니다"

    # 데이터 안전을 위한 MySQL 정상 종료 대기
    echo "   💾 MySQL 데이터 정리 대기 중..."
    if docker-compose -f compose.yaml ps mysql | grep -q "Up"; then
        # MySQL 컨테이너에 정상 종료 신호
        docker-compose -f compose.yaml exec mysql mysqladmin -u root -p${MYSQL_ROOT_PASSWORD:-root} shutdown 2>/dev/null || true
        sleep 3
    fi

    # Docker Compose 서비스 종료
    docker-compose -f compose.yaml down --timeout 30

    # MySQL 관련 컨테이너 정리
    echo "   🧹 MySQL 관련 리소스 정리 중..."

    # 정지된 MySQL 컨테이너 제거
    STOPPED_CONTAINERS=$(docker ps -a -q --filter "name=mysql" --filter "status=exited" 2>/dev/null || true)
    if [ ! -z "$STOPPED_CONTAINERS" ]; then
        docker rm $STOPPED_CONTAINERS 2>/dev/null || true
        echo "   ✅ 정지된 MySQL 컨테이너 제거 완료"
    fi

    echo "   ✅ MySQL 서버 종료 완료"
else
    echo "   ❌ compose.yaml 파일이 없습니다"
fi
