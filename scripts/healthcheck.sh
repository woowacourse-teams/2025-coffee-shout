#!/bin/bash

################################################################################
# CoffeeShout 헬스체크 스크립트
# Usage: ./healthcheck.sh [all|was|mysql|valkey|monitoring]
################################################################################

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 환경 변수
CHECK_TYPE=${1:-all}
DEPLOY_PATH="/opt/coffee-shout"
WAS_URL="http://localhost:8080"
GRAFANA_URL="http://localhost:3000"
PROMETHEUS_URL="http://localhost:9090"

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

log_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

check_was() {
    log_header "WAS Health Check"

    # Process check
    PID=$(pgrep -f "coffee-shout-backend.jar" || echo "")

    if [ -n "$PID" ]; then
        log_info "✅ WAS process is running (PID: $PID)"

        # Memory usage
        MEM=$(ps -p "$PID" -o %mem --no-headers)
        log_info "   Memory usage: ${MEM}%"

        # CPU usage
        CPU=$(ps -p "$PID" -o %cpu --no-headers)
        log_info "   CPU usage: ${CPU}%"
    else
        log_error "❌ WAS process is not running"
        return 1
    fi

    # HTTP health check
    if curl -f -s "$WAS_URL/actuator/health" > /dev/null 2>&1; then
        log_info "✅ WAS HTTP endpoint is responding"

        # Detailed health info
        HEALTH=$(curl -s "$WAS_URL/actuator/health")
        echo "$HEALTH" | jq '.' 2>/dev/null || echo "$HEALTH"
    else
        log_error "❌ WAS HTTP endpoint is not responding"
        return 1
    fi

    # Additional metrics
    log_info ""
    log_info "Additional metrics:"

    # Uptime
    if command -v jq &> /dev/null; then
        UPTIME=$(curl -s "$WAS_URL/actuator/metrics/process.uptime" | jq -r '.measurements[0].value')
        UPTIME_MIN=$(echo "$UPTIME / 60" | bc)
        log_info "   Uptime: ${UPTIME_MIN} minutes"
    fi

    # Thread count
    THREADS=$(curl -s "$WAS_URL/actuator/metrics/jvm.threads.live" | jq -r '.measurements[0].value' 2>/dev/null || echo "N/A")
    log_info "   Active threads: $THREADS"

    return 0
}

check_mysql() {
    log_header "MySQL Health Check"

    cd "$DEPLOY_PATH"

    if ! docker compose ps mysql | grep -q "Up"; then
        log_error "❌ MySQL container is not running"
        return 1
    fi

    log_info "✅ MySQL container is running"

    # Load environment variables
    if [ -f .env ]; then
        source .env
    fi

    # Connection test
    if docker compose exec -T mysql mysqladmin ping -h localhost -u root -p"${MYSQL_ROOT_PASSWORD}" > /dev/null 2>&1; then
        log_info "✅ MySQL is responding to connections"
    else
        log_error "❌ MySQL is not responding"
        return 1
    fi

    # Database status
    log_info ""
    log_info "MySQL status:"

    STATUS=$(docker compose exec -T mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "SHOW STATUS LIKE 'Threads_connected';" 2>/dev/null | tail -1)
    log_info "   $STATUS"

    STATUS=$(docker compose exec -T mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "SHOW STATUS LIKE 'Uptime';" 2>/dev/null | tail -1)
    log_info "   $STATUS"

    # Database size
    DB_SIZE=$(docker compose exec -T mysql mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "
        SELECT
            table_schema AS 'Database',
            ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
        FROM information_schema.tables
        WHERE table_schema NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')
        GROUP BY table_schema;
    " 2>/dev/null)

    log_info ""
    log_info "Database sizes:"
    echo "$DB_SIZE"

    return 0
}

check_valkey() {
    log_header "Valkey Health Check"

    cd "$DEPLOY_PATH"

    if ! docker compose ps valkey | grep -q "Up"; then
        log_error "❌ Valkey container is not running"
        return 1
    fi

    log_info "✅ Valkey container is running"

    # Connection test
    if docker compose exec valkey valkey-cli ping | grep -q "PONG"; then
        log_info "✅ Valkey is responding to connections"
    else
        log_error "❌ Valkey is not responding"
        return 1
    fi

    # Memory usage
    log_info ""
    log_info "Valkey info:"

    MEMORY=$(docker compose exec valkey valkey-cli INFO memory | grep "used_memory_human")
    log_info "   $MEMORY"

    KEYS=$(docker compose exec valkey valkey-cli DBSIZE)
    log_info "   Keys: $KEYS"

    UPTIME=$(docker compose exec valkey valkey-cli INFO server | grep "uptime_in_seconds")
    log_info "   $UPTIME"

    return 0
}

check_monitoring() {
    log_header "Monitoring Stack Health Check"

    # Grafana
    if curl -f -s "$GRAFANA_URL/api/health" > /dev/null 2>&1; then
        log_info "✅ Grafana is running"
    else
        log_warn "⚠️ Grafana is not responding"
    fi

    # Prometheus
    if curl -f -s "$PROMETHEUS_URL/-/healthy" > /dev/null 2>&1; then
        log_info "✅ Prometheus is running"

        # Target status
        TARGETS=$(curl -s "$PROMETHEUS_URL/api/v1/targets" | jq -r '.data.activeTargets | length' 2>/dev/null || echo "N/A")
        log_info "   Active targets: $TARGETS"
    else
        log_warn "⚠️ Prometheus is not responding"
    fi

    # Tempo
    if curl -f -s "http://localhost:3200/ready" > /dev/null 2>&1; then
        log_info "✅ Tempo is running"
    else
        log_warn "⚠️ Tempo is not responding"
    fi

    return 0
}

show_system_info() {
    log_header "System Information"

    # CPU usage
    log_info "CPU usage:"
    top -bn1 | grep "Cpu(s)" | head -1

    # Memory usage
    log_info ""
    log_info "Memory usage:"
    free -h

    # Disk usage
    log_info ""
    log_info "Disk usage:"
    df -h / | tail -1

    # Docker resources
    log_info ""
    log_info "Docker container resources:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || log_warn "Docker not available"
}

################################################################################
# 메인 로직
################################################################################

main() {
    log_header "CoffeeShout Health Check"
    echo ""

    ALL_GOOD=true

    case "$CHECK_TYPE" in
        was)
            check_was || ALL_GOOD=false
            ;;
        mysql)
            check_mysql || ALL_GOOD=false
            ;;
        valkey|redis)
            check_valkey || ALL_GOOD=false
            ;;
        monitoring)
            check_monitoring || ALL_GOOD=false
            ;;
        all)
            check_was || ALL_GOOD=false
            echo ""
            check_mysql || ALL_GOOD=false
            echo ""
            check_valkey || ALL_GOOD=false
            echo ""
            show_system_info
            ;;
        *)
            log_error "Invalid check type: $CHECK_TYPE"
            log_info "Usage: ./healthcheck.sh [all|was|mysql|valkey|monitoring]"
            exit 1
            ;;
    esac

    echo ""
    log_header "Health Check Summary"

    if [ "$ALL_GOOD" = true ]; then
        log_info "✅ All checks passed!"
        exit 0
    else
        log_error "❌ Some checks failed!"
        exit 1
    fi
}

# 스크립트 실행
main
