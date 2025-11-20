#!/bin/bash

################################################################################
# CoffeeShout 배포 스크립트
# Usage: ./deploy.sh [dev|prod]
################################################################################

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수
ENVIRONMENT=${1:-dev}
DEPLOY_PATH="/opt/coffee-shout"
JAR_NAME="coffee-shout-backend.jar"
LOG_PATH="$DEPLOY_PATH/logs"
BACKUP_PATH="$DEPLOY_PATH/backup"

################################################################################
# 함수 정의
################################################################################

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_environment() {
    if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
        log_error "Invalid environment: $ENVIRONMENT"
        log_info "Usage: ./deploy.sh [dev|prod]"
        exit 1
    fi
    log_info "Environment: $ENVIRONMENT"
}

check_directories() {
    log_info "Checking directories..."
    mkdir -p "$DEPLOY_PATH" "$LOG_PATH" "$BACKUP_PATH"
}

check_jar_file() {
    if [ ! -f "$DEPLOY_PATH/$JAR_NAME" ]; then
        log_error "JAR file not found: $DEPLOY_PATH/$JAR_NAME"
        exit 1
    fi
    log_info "JAR file found: $JAR_NAME"
}

backup_current_jar() {
    if [ -f "$DEPLOY_PATH/$JAR_NAME" ]; then
        BACKUP_NAME="$JAR_NAME.$(date +%Y%m%d_%H%M%S)"
        log_info "Backing up current JAR to: $BACKUP_NAME"
        cp "$DEPLOY_PATH/$JAR_NAME" "$BACKUP_PATH/$BACKUP_NAME"
        log_info "Backup completed"
    else
        log_warn "No existing JAR to backup"
    fi
}

stop_application() {
    log_info "Stopping application..."

    PID=$(pgrep -f "$JAR_NAME" || echo "")

    if [ -n "$PID" ]; then
        log_info "Found running application (PID: $PID)"

        # Graceful shutdown
        log_info "Sending SIGTERM signal for graceful shutdown..."
        kill -SIGTERM "$PID" || true

        # 30초 대기
        for i in {1..30}; do
            if ! pgrep -f "$JAR_NAME" > /dev/null; then
                log_info "Application stopped gracefully"
                return 0
            fi
            echo -n "."
            sleep 1
        done
        echo ""

        # Force kill
        log_warn "Graceful shutdown timeout, force killing..."
        pkill -9 -f "$JAR_NAME" || true
        sleep 2

        if pgrep -f "$JAR_NAME" > /dev/null; then
            log_error "Failed to stop application"
            exit 1
        fi
    else
        log_info "No running application found"
    fi
}

start_application() {
    log_info "Starting application..."

    cd "$DEPLOY_PATH"

    # JVM 옵션 설정
    if [ "$ENVIRONMENT" = "prod" ]; then
        JVM_OPTS="-Xms1024m -Xmx1536m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
    else
        JVM_OPTS="-Xms512m -Xmx1024m"
    fi

    # 애플리케이션 시작
    nohup java -jar \
        -Dspring.profiles.active="$ENVIRONMENT" \
        $JVM_OPTS \
        "$JAR_NAME" \
        > "$LOG_PATH/application.log" 2>&1 &

    NEW_PID=$!
    log_info "Application started (PID: $NEW_PID)"
}

health_check() {
    log_info "Performing health check..."

    MAX_ATTEMPTS=36
    ATTEMPT=0

    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        ATTEMPT=$((ATTEMPT + 1))

        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_info "✅ Application is healthy!"
            curl -s http://localhost:8080/actuator/health | jq '.' || true
            return 0
        fi

        echo -n "⏳ Waiting for application to start... ($ATTEMPT/$MAX_ATTEMPTS)"
        sleep 5
        echo ""
    done

    log_error "❌ Health check failed!"
    return 1
}

rollback() {
    log_error "Deployment failed, rolling back..."

    LATEST_BACKUP=$(ls -t "$BACKUP_PATH"/*.jar 2>/dev/null | head -1)

    if [ -n "$LATEST_BACKUP" ]; then
        log_info "Found backup: $LATEST_BACKUP"

        # Stop failed application
        pkill -9 -f "$JAR_NAME" || true

        # Restore backup
        cp "$LATEST_BACKUP" "$DEPLOY_PATH/$JAR_NAME"
        log_info "Backup restored"

        # Start application
        start_application

        # Health check
        if health_check; then
            log_info "✅ Rollback successful"
            exit 0
        else
            log_error "❌ Rollback failed"
            exit 1
        fi
    else
        log_error "No backup found for rollback"
        exit 1
    fi
}

cleanup_old_backups() {
    log_info "Cleaning up old backups (older than 7 days)..."
    find "$BACKUP_PATH" -name "*.jar" -mtime +7 -delete
    log_info "Cleanup completed"
}

################################################################################
# 메인 로직
################################################################################

main() {
    log_info "================================"
    log_info "CoffeeShout Deployment Script"
    log_info "================================"

    check_environment
    check_directories
    check_jar_file
    backup_current_jar
    stop_application
    start_application

    if health_check; then
        log_info "✅ Deployment successful!"
        cleanup_old_backups
        exit 0
    else
        rollback
    fi
}

# 스크립트 실행
main
