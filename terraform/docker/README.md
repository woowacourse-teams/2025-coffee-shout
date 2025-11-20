# Docker Compose 설정

이 디렉토리는 CoffeeShout 인프라의 Docker Compose 설정 파일들을 포함합니다.

## 📁 파일 구조

```
terraform/docker/
├── dev-docker-compose.yml           # DEV 환경 (MySQL + Valkey)
├── prod-docker-compose.yml          # PROD 환경 (MySQL + Valkey)
├── monitoring-docker-compose.yml    # 모니터링 (Grafana + Prometheus + Tempo)
├── prometheus/
│   └── prometheus.yml               # Prometheus 설정
├── tempo/
│   └── tempo.yml                    # Tempo 설정
├── grafana/
│   └── provisioning/
│       └── datasources/
│           └── datasources.yml      # Grafana 데이터소스
└── README.md
```

---

## 🚀 사용법

### 1. DEV 환경

#### 환경변수 설정

```bash
# .env.dev 파일 생성
cat > .env.dev <<EOF
MYSQL_ROOT_PASSWORD=dev_root_password_2025
MYSQL_DATABASE=coffee_shout_dev
MYSQL_USER=coffeeshout
MYSQL_PASSWORD=dev_password_2025
EOF
```

#### 실행

```bash
# DEV 서버에 접속
ssh dev-server

# Docker Compose 파일 복사
scp dev-docker-compose.yml dev-server:/opt/coffee-shout/docker-compose.yml
scp .env.dev dev-server:/opt/coffee-shout/.env

# 컨테이너 시작
cd /opt/coffee-shout
docker compose up -d

# 상태 확인
docker compose ps
docker compose logs -f
```

#### 접속 정보

- MySQL: `localhost:3306`
- Valkey: `localhost:6379`

---

### 2. PROD 환경

#### 환경변수 설정

```bash
# .env.prod 파일 생성 (보안 주의!)
cat > .env.prod <<EOF
MYSQL_ROOT_PASSWORD=STRONG_PASSWORD_HERE
MYSQL_DATABASE=coffee_shout
MYSQL_USER=coffeeshout
MYSQL_PASSWORD=STRONG_USER_PASSWORD_HERE
EOF

# 파일 권한 설정
chmod 600 .env.prod
```

#### 실행

```bash
# PROD 서버에 접속
ssh prod-server

# Docker Compose 파일 복사
scp prod-docker-compose.yml prod-server:/opt/coffee-shout/docker-compose.yml
scp .env.prod prod-server:/opt/coffee-shout/.env

# 컨테이너 시작
cd /opt/coffee-shout
docker compose up -d

# 상태 확인
docker compose ps
docker compose logs -f
```

#### 접속 정보

- MySQL: `localhost:3306`
- Valkey: `localhost:6379`

---

### 3. Monitoring 환경

#### 환경변수 설정

```bash
# .env.monitoring 파일 생성
cat > .env.monitoring <<EOF
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=CHANGE_ME
GRAFANA_ROOT_URL=http://monitoring.example.com:3000
GRAFANA_DOMAIN=monitoring.example.com
EOF
```

#### Prometheus 설정 수정

```bash
# prometheus/prometheus.yml 파일에서 IP 주소 변경
vi prometheus/prometheus.yml

# DEV_SERVER_IP, PROD_SERVER_IP를 실제 IP로 변경
# 예: DEV_SERVER_IP -> 10.0.1.100
#     PROD_SERVER_IP -> 10.0.2.100
```

#### 실행

```bash
# Monitoring 서버에 접속
ssh monitoring-server

# 설정 파일 복사
scp -r . monitoring-server:/opt/monitoring/

# 컨테이너 시작
cd /opt/monitoring
docker compose -f monitoring-docker-compose.yml up -d

# 상태 확인
docker compose -f monitoring-docker-compose.yml ps
docker compose -f monitoring-docker-compose.yml logs -f
```

#### 접속 정보

- Grafana: `http://monitoring-server:3000`
- Prometheus: `http://monitoring-server:9090`
- Tempo: `http://monitoring-server:3200`

---

## 🔧 관리 명령어

### 컨테이너 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker compose ps

# 로그 확인 (실시간)
docker compose logs -f

# 특정 서비스 로그만 확인
docker compose logs -f mysql
docker compose logs -f valkey
```

### 컨테이너 재시작

```bash
# 전체 재시작
docker compose restart

# 특정 서비스만 재시작
docker compose restart mysql
docker compose restart valkey
```

### 컨테이너 중지/시작

```bash
# 중지 (데이터 보존)
docker compose stop

# 시작
docker compose start

# 완전 삭제 (데이터 삭제!)
docker compose down -v
```

### 리소스 사용량 확인

```bash
# 컨테이너별 리소스 사용량
docker stats

# 디스크 사용량
docker system df
```

---

## 📊 리소스 할당

### DEV 환경 (t4g.small: 2GB RAM)

| 서비스 | 메모리 제한 | 메모리 예약 |
|--------|------------|------------|
| MySQL | 512MB | 256MB |
| Valkey | 256MB | 128MB |
| WAS (Spring Boot) | ~1GB | - |
| 시스템 | ~256MB | - |
| **합계** | **2GB** | - |

### PROD 환경 (t4g.small: 2GB RAM)

| 서비스 | 메모리 제한 | 메모리 예약 |
|--------|------------|------------|
| MySQL | 768MB | 512MB |
| Valkey | 384MB | 256MB |
| WAS (Spring Boot) | ~768MB | - |
| 시스템 | ~128MB | - |
| **합계** | **2GB** | - |

### Monitoring 환경 (t4g.small: 2GB RAM)

| 서비스 | 메모리 제한 | 메모리 예약 |
|--------|------------|------------|
| Prometheus | 768MB | 512MB |
| Grafana | 512MB | 256MB |
| Tempo | 512MB | 256MB |
| Node Exporter | 128MB | 64MB |
| 시스템 | ~80MB | - |
| **합계** | **2GB** | - |

---

## 🔐 보안 권장사항

### 1. 환경변수 파일 보호

```bash
# .env 파일 권한 설정
chmod 600 .env*

# Git에서 제외
echo ".env*" >> .gitignore
```

### 2. 비밀번호 변경

```bash
# MySQL root 비밀번호 변경
docker compose exec mysql mysql -u root -p
> ALTER USER 'root'@'%' IDENTIFIED BY 'NEW_PASSWORD';
> FLUSH PRIVILEGES;
```

### 3. 방화벽 설정

```bash
# MySQL/Valkey는 localhost만 접근 허용
# Security Group에서 EC2 내부 통신만 허용
```

---

## 🔄 백업 및 복구

### MySQL 백업

```bash
# 백업 스크립트 실행
docker compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} \
  --all-databases --single-transaction --quick --lock-tables=false \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# S3에 업로드
aws s3 cp backup_*.sql s3://coffee-shout-backup/mysql/
```

### MySQL 복구

```bash
# 백업에서 복구
docker compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} \
  < backup_20250120_020000.sql
```

### Valkey 백업

```bash
# RDB 파일 백업
docker compose exec valkey valkey-cli BGSAVE
docker cp coffeeshout-valkey-prod:/data/dump.rdb ./valkey_backup_$(date +%Y%m%d).rdb

# S3에 업로드
aws s3 cp valkey_backup_*.rdb s3://coffee-shout-backup/valkey/
```

---

## 🐛 문제 해결

### MySQL 컨테이너가 시작되지 않을 때

```bash
# 로그 확인
docker compose logs mysql

# 일반적인 원인:
# 1. 메모리 부족 -> 메모리 제한 줄이기
# 2. 포트 충돌 -> 포트 변경
# 3. 데이터 손상 -> 볼륨 삭제 후 재생성
```

### Valkey 메모리 초과

```bash
# 메모리 사용량 확인
docker compose exec valkey valkey-cli INFO memory

# 캐시 삭제 (주의!)
docker compose exec valkey valkey-cli FLUSHALL
```

### 디스크 공간 부족

```bash
# Docker 정리
docker system prune -a --volumes

# 로그 파일 정리
docker compose exec mysql sh -c "rm -rf /var/log/mysql/*.log.*"
```

---

## 📚 추가 문서

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [MySQL 8.0 문서](https://dev.mysql.com/doc/refman/8.0/en/)
- [Valkey 문서](https://valkey.io/docs/)
- [Grafana 문서](https://grafana.com/docs/)
- [Prometheus 문서](https://prometheus.io/docs/)
