# 배포 및 관리 스크립트

이 디렉토리는 CoffeeShout 인프라 관리를 위한 유틸리티 스크립트들을 포함합니다.

## 📁 파일 구조

```
scripts/
├── deploy.sh        # 배포 스크립트
├── backup.sh        # 백업 스크립트
├── healthcheck.sh   # 헬스체크 스크립트
└── README.md
```

---

## 🚀 deploy.sh

애플리케이션 배포 스크립트입니다.

### 기능

- 기존 JAR 파일 백업
- Graceful shutdown
- 새 버전 시작
- 헬스체크
- 실패 시 자동 롤백

### 사용법

```bash
# DEV 환경 배포
./deploy.sh dev

# PROD 환경 배포
./deploy.sh prod
```

### 전제 조건

- JAR 파일이 `/opt/coffee-shout/coffee-shout-backend.jar`에 존재
- `curl`, `jq` 설치 필요
- Spring Boot Actuator 활성화 (헬스체크용)

### 동작 과정

1. 환경 검증 (dev/prod)
2. 디렉토리 확인 및 생성
3. JAR 파일 존재 확인
4. 현재 JAR 백업
5. 기존 애플리케이션 Graceful Shutdown (최대 30초)
6. 새 애플리케이션 시작
7. 헬스체크 (최대 3분, 5초 간격)
8. 성공: 7일 이상 된 백업 삭제
9. 실패: 자동 롤백

### 예시

```bash
$ ./deploy.sh prod
[INFO] ================================
[INFO] CoffeeShout Deployment Script
[INFO] ================================
[INFO] Environment: prod
[INFO] Checking directories...
[INFO] JAR file found: coffee-shout-backend.jar
[INFO] Backing up current JAR to: coffee-shout-backend.jar.20250120_120000
[INFO] Backup completed
[INFO] Stopping application...
[INFO] Found running application (PID: 12345)
[INFO] Sending SIGTERM signal for graceful shutdown...
[INFO] Application stopped gracefully
[INFO] Starting application...
[INFO] Application started (PID: 23456)
[INFO] Performing health check...
[INFO] ✅ Application is healthy!
[INFO] ✅ Deployment successful!
[INFO] Cleaning up old backups (older than 7 days)...
[INFO] Cleanup completed
```

---

## 💾 backup.sh

데이터베이스 백업 스크립트입니다.

### 기능

- MySQL 전체 데이터베이스 백업
- Valkey (Redis) RDB 파일 백업
- 자동 압축 (gzip)
- S3 업로드 (선택사항)
- 7일 이상 된 백업 자동 삭제

### 사용법

```bash
# MySQL만 백업
./backup.sh mysql

# Valkey만 백업
./backup.sh valkey

# 모두 백업 (기본)
./backup.sh all
```

### 전제 조건

- Docker Compose 실행 중
- `/opt/coffee-shout/.env` 파일에 MySQL 비밀번호 설정
- AWS CLI 설치 (S3 업로드 시)

### 백업 파일 위치

```
/opt/coffee-shout/backup/
├── mysql_backup_20250120_120000.sql.gz
├── mysql_backup_20250119_120000.sql.gz
├── valkey_dump_20250120_120000.rdb.gz
└── valkey_dump_20250119_120000.rdb.gz
```

### S3 업로드 설정

```bash
# 환경 변수 설정
export S3_BACKUP_BUCKET="your-backup-bucket"

# 스크립트 실행 시 자동으로 S3에 업로드됨
./backup.sh all
```

### Cron 자동 백업

```bash
# crontab 편집
crontab -e

# 매일 새벽 2시 백업
0 2 * * * /opt/coffee-shout/scripts/backup.sh all >> /opt/coffee-shout/logs/backup.log 2>&1
```

### 예시

```bash
$ ./backup.sh all
[INFO] ================================
[INFO] CoffeeShout Backup Script
[INFO] ================================
[INFO] Backup type: all
[INFO] Timestamp: 20250120_120000
[INFO] Checking backup directory...
[INFO] Starting MySQL backup...
[INFO] ✅ MySQL backup completed: /opt/coffee-shout/backup/mysql_backup_20250120_120000.sql.gz
[INFO] Uploading to S3: s3://coffee-shout-backup/mysql/mysql_backup_20250120_120000.sql.gz
[INFO] ✅ S3 upload successful
[INFO] Starting Valkey (Redis) backup...
[INFO] Waiting for BGSAVE to complete...
[INFO] BGSAVE completed
[INFO] ✅ Valkey backup completed: /opt/coffee-shout/backup/valkey_dump_20250120_120000.rdb.gz
[INFO] Uploading to S3: s3://coffee-shout-backup/valkey/valkey_dump_20250120_120000.rdb.gz
[INFO] ✅ S3 upload successful
[INFO] Cleaning up old backups (older than 7 days)...
[INFO] Cleanup completed
[INFO] ================================
[INFO] Backup Summary
[INFO] ================================
[INFO] Recent backups:
-rw-r--r-- 1 ubuntu ubuntu 12M Jan 20 12:00 mysql_backup_20250120_120000.sql.gz
-rw-r--r-- 1 ubuntu ubuntu 256K Jan 20 12:00 valkey_dump_20250120_120000.rdb.gz
[INFO] Disk usage:
13M     /opt/coffee-shout/backup
[INFO] ✅ Backup process completed
```

---

## 🏥 healthcheck.sh

시스템 헬스체크 스크립트입니다.

### 기능

- WAS (Spring Boot) 상태 확인
- MySQL 상태 확인
- Valkey (Redis) 상태 확인
- 모니터링 스택 상태 확인 (Grafana, Prometheus, Tempo)
- 시스템 리소스 확인

### 사용법

```bash
# 전체 헬스체크
./healthcheck.sh all

# WAS만 확인
./healthcheck.sh was

# MySQL만 확인
./healthcheck.sh mysql

# Valkey만 확인
./healthcheck.sh valkey

# 모니터링 스택 확인
./healthcheck.sh monitoring
```

### 전제 조건

- `curl`, `jq`, `bc` 설치 필요
- Spring Boot Actuator 활성화
- Docker Compose 실행 중

### 확인 항목

#### WAS
- 프로세스 실행 여부
- HTTP 엔드포인트 응답
- 메모리/CPU 사용량
- Uptime, 활성 스레드 수

#### MySQL
- 컨테이너 실행 여부
- 연결 가능 여부
- 접속 중인 스레드 수
- Uptime, 데이터베이스 크기

#### Valkey
- 컨테이너 실행 여부
- PING 응답
- 메모리 사용량
- 키 개수, Uptime

#### 시스템
- CPU 사용률
- 메모리 사용량
- 디스크 사용량
- Docker 컨테이너 리소스

### Cron 자동 모니터링

```bash
# crontab 편집
crontab -e

# 5분마다 헬스체크 (실패 시에만 알림)
*/5 * * * * /opt/coffee-shout/scripts/healthcheck.sh all || echo "Health check failed!" | mail -s "CoffeeShout Alert" admin@example.com
```

### 예시

```bash
$ ./healthcheck.sh all
================================
CoffeeShout Health Check
================================

================================
WAS Health Check
================================
[INFO] ✅ WAS process is running (PID: 12345)
[INFO]    Memory usage: 45.2%
[INFO]    CPU usage: 12.5%
[INFO] ✅ WAS HTTP endpoint is responding
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
[INFO]
[INFO] Additional metrics:
[INFO]    Uptime: 120 minutes
[INFO]    Active threads: 25

================================
MySQL Health Check
================================
[INFO] ✅ MySQL container is running
[INFO] ✅ MySQL is responding to connections
[INFO]
[INFO] MySQL status:
Threads_connected       15
Uptime  7200
[INFO]
[INFO] Database sizes:
Database        Size (MB)
coffee_shout    125.50

================================
Valkey Health Check
================================
[INFO] ✅ Valkey container is running
[INFO] ✅ Valkey is responding to connections
[INFO]
[INFO] Valkey info:
used_memory_human:64.25M
[INFO]    Keys: (integer) 1523
[INFO]    uptime_in_seconds:7200

================================
System Information
================================
[INFO] CPU usage:
%Cpu(s): 15.2 us,  2.1 sy,  0.0 ni, 82.1 id,  0.3 wa,  0.0 hi,  0.3 si,  0.0 st
[INFO]
[INFO] Memory usage:
              total        used        free      shared  buff/cache   available
Mem:           1.9Gi       1.2Gi       200Mi        10Mi       500Mi       600Mi
[INFO]
[INFO] Disk usage:
/dev/xvda1       20G   12G  7.5G  62% /
[INFO]
[INFO] Docker container resources:
NAME                        CPU %     MEM USAGE / LIMIT
coffeeshout-mysql-prod      5.23%     512MiB / 768MiB
coffeeshout-valkey-prod     1.05%     64MiB / 384MiB

================================
Health Check Summary
================================
[INFO] ✅ All checks passed!
```

---

## 🔧 설치 가이드

### 1. 스크립트 배포

```bash
# 서버에 스크립트 복사
scp scripts/*.sh server:/opt/coffee-shout/scripts/

# 실행 권한 부여
ssh server "chmod +x /opt/coffee-shout/scripts/*.sh"
```

### 2. 필수 패키지 설치

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y curl jq bc

# Amazon Linux 2
sudo yum install -y curl jq bc
```

### 3. Cron 작업 등록

```bash
# crontab 편집
crontab -e

# 추가할 내용:
# 매일 새벽 2시 백업
0 2 * * * /opt/coffee-shout/scripts/backup.sh all >> /opt/coffee-shout/logs/backup.log 2>&1

# 5분마다 헬스체크
*/5 * * * * /opt/coffee-shout/scripts/healthcheck.sh all >> /opt/coffee-shout/logs/healthcheck.log 2>&1
```

---

## 🐛 문제 해결

### 스크립트 실행 권한 오류

```bash
chmod +x /opt/coffee-shout/scripts/*.sh
```

### jq 명령어 없음

```bash
sudo apt-get install jq
```

### Docker 명령어 권한 오류

```bash
sudo usermod -aG docker $USER
# 로그아웃 후 다시 로그인
```

### 백업 파일 용량 부족

```bash
# 오래된 백업 수동 삭제
find /opt/coffee-shout/backup -name "*.gz" -mtime +7 -delete
```

---

## 📚 추가 문서

- [배포 가이드](../terraform/environments/README.md)
- [Docker Compose 가이드](../terraform/docker/README.md)
- [GitHub Actions 워크플로우](../.github/workflows/)
