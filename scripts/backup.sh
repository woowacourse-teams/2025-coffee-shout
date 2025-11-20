#!/bin/bash

################################################################################
# CoffeeShout 백업 스크립트
# Usage: ./backup.sh [mysql|valkey|all]
################################################################################

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 환경 변수
BACKUP_TYPE=${1:-all}
DEPLOY_PATH="/opt/coffee-shout"
BACKUP_PATH="$DEPLOY_PATH/backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
S3_BUCKET=${S3_BACKUP_BUCKET:-"coffee-shout-backup"}

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

check_directories() {
    log_info "Checking backup directory..."
    mkdir -p "$BACKUP_PATH"
}

backup_mysql() {
    log_info "Starting MySQL backup..."

    BACKUP_FILE="$BACKUP_PATH/mysql_backup_$TIMESTAMP.sql"

    # Docker Compose를 통해 MySQL 백업
    cd "$DEPLOY_PATH"

    if ! docker compose ps mysql | grep -q "Up"; then
        log_error "MySQL container is not running"
        return 1
    fi

    # 환경 변수 로드
    if [ -f .env ]; then
        source .env
    else
        log_error ".env file not found"
        return 1
    fi

    # mysqldump 실행
    docker compose exec -T mysql mysqldump \
        -u root \
        -p"${MYSQL_ROOT_PASSWORD}" \
        --all-databases \
        --single-transaction \
        --quick \
        --lock-tables=false \
        --routines \
        --triggers \
        --events \
        > "$BACKUP_FILE"

    if [ -f "$BACKUP_FILE" ]; then
        # 압축
        gzip "$BACKUP_FILE"
        log_info "✅ MySQL backup completed: ${BACKUP_FILE}.gz"

        # S3 업로드 (optional)
        upload_to_s3 "${BACKUP_FILE}.gz" "mysql/"
    else
        log_error "❌ MySQL backup failed"
        return 1
    fi
}

backup_valkey() {
    log_info "Starting Valkey (Redis) backup..."

    BACKUP_FILE="$BACKUP_PATH/valkey_dump_$TIMESTAMP.rdb"

    cd "$DEPLOY_PATH"

    if ! docker compose ps valkey | grep -q "Up"; then
        log_error "Valkey container is not running"
        return 1
    fi

    # BGSAVE 실행
    docker compose exec valkey valkey-cli BGSAVE

    # BGSAVE 완료 대기
    log_info "Waiting for BGSAVE to complete..."
    for i in {1..30}; do
        LASTSAVE=$(docker compose exec valkey valkey-cli LASTSAVE | tr -d '\r')
        sleep 1
        LASTSAVE_NEW=$(docker compose exec valkey valkey-cli LASTSAVE | tr -d '\r')

        if [ "$LASTSAVE" != "$LASTSAVE_NEW" ]; then
            log_info "BGSAVE completed"
            break
        fi

        if [ $i -eq 30 ]; then
            log_warn "BGSAVE timeout, continuing anyway..."
        fi
    done

    # RDB 파일 복사
    CONTAINER_NAME=$(docker compose ps -q valkey)
    docker cp "$CONTAINER_NAME:/data/dump.rdb" "$BACKUP_FILE"

    if [ -f "$BACKUP_FILE" ]; then
        # 압축
        gzip "$BACKUP_FILE"
        log_info "✅ Valkey backup completed: ${BACKUP_FILE}.gz"

        # S3 업로드 (optional)
        upload_to_s3 "${BACKUP_FILE}.gz" "valkey/"
    else
        log_error "❌ Valkey backup failed"
        return 1
    fi
}

upload_to_s3() {
    local FILE=$1
    local PREFIX=$2

    if ! command -v aws &> /dev/null; then
        log_warn "AWS CLI not found, skipping S3 upload"
        return 0
    fi

    log_info "Uploading to S3: s3://$S3_BUCKET/$PREFIX$(basename $FILE)"

    if aws s3 cp "$FILE" "s3://$S3_BUCKET/$PREFIX" --region ap-northeast-2; then
        log_info "✅ S3 upload successful"
    else
        log_warn "⚠️ S3 upload failed"
    fi
}

cleanup_old_backups() {
    log_info "Cleaning up old backups (older than 7 days)..."

    find "$BACKUP_PATH" -name "mysql_backup_*.sql.gz" -mtime +7 -delete
    find "$BACKUP_PATH" -name "valkey_dump_*.rdb.gz" -mtime +7 -delete

    log_info "Cleanup completed"
}

show_backup_info() {
    log_info "================================"
    log_info "Backup Summary"
    log_info "================================"

    log_info "Recent backups:"
    ls -lh "$BACKUP_PATH"/*.gz 2>/dev/null | tail -10 || echo "No backups found"

    log_info ""
    log_info "Disk usage:"
    du -sh "$BACKUP_PATH"
}

################################################################################
# 메인 로직
################################################################################

main() {
    log_info "================================"
    log_info "CoffeeShout Backup Script"
    log_info "================================"
    log_info "Backup type: $BACKUP_TYPE"
    log_info "Timestamp: $TIMESTAMP"

    check_directories

    case "$BACKUP_TYPE" in
        mysql)
            backup_mysql
            ;;
        valkey|redis)
            backup_valkey
            ;;
        all)
            backup_mysql
            backup_valkey
            ;;
        *)
            log_error "Invalid backup type: $BACKUP_TYPE"
            log_info "Usage: ./backup.sh [mysql|valkey|all]"
            exit 1
            ;;
    esac

    cleanup_old_backups
    show_backup_info

    log_info "✅ Backup process completed"
}

# 스크립트 실행
main
