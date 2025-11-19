#!/bin/bash
set -e

# 시스템 업데이트
dnf update -y

# Docker 설치
dnf install -y docker
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

# Docker Compose 설치
DOCKER_COMPOSE_VERSION="2.24.5"
curl -L "https://github.com/docker/compose/releases/download/v${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# EBS 볼륨 마운트 대기 (최대 60초)
echo "Waiting for EBS volume to attach..."
DEVICE="/dev/nvme1n1"
MAX_WAIT=60
ELAPSED=0

while [ ! -b "$DEVICE" ] && [ $ELAPSED -lt $MAX_WAIT ]; do
    sleep 2
    ELAPSED=$((ELAPSED + 2))
done

if [ ! -b "$DEVICE" ]; then
    echo "ERROR: EBS volume not found at $DEVICE"
    exit 1
fi

# 파일시스템 확인 및 생성
if ! blkid "$DEVICE"; then
    echo "Creating filesystem on $DEVICE..."
    mkfs -t ext4 "$DEVICE"
fi

# 마운트 포인트 생성 및 마운트
mkdir -p /mnt/monitoring-data
mount "$DEVICE" /mnt/monitoring-data

# 영구 마운트 설정 (UUID 사용)
UUID=$(blkid -s UUID -o value "$DEVICE")
if ! grep -q "$UUID" /etc/fstab; then
    echo "UUID=$UUID /mnt/monitoring-data ext4 defaults,nofail 0 2" >> /etc/fstab
fi

# 모니터링 데이터 디렉토리 생성
mkdir -p /mnt/monitoring-data/grafana
mkdir -p /mnt/monitoring-data/prometheus
mkdir -p /mnt/monitoring-data/loki
mkdir -p /mnt/monitoring-data/tempo

# 권한 설정 (Docker 컨테이너에서 접근 가능하도록)
chown -R 472:472 /mnt/monitoring-data/grafana  # Grafana UID/GID
chown -R 65534:65534 /mnt/monitoring-data/prometheus  # nobody UID/GID
chown -R 10001:10001 /mnt/monitoring-data/loki  # Loki UID/GID
chown -R 10001:10001 /mnt/monitoring-data/tempo  # Tempo UID/GID

# Docker Compose 파일 생성
cat > /home/ec2-user/docker-compose.yml <<'EOF'
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - /mnt/monitoring-data/prometheus:/prometheus
      - /home/ec2-user/prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    restart: unless-stopped
    networks:
      - monitoring

  loki:
    image: grafana/loki:latest
    container_name: loki
    ports:
      - "3100:3100"
    volumes:
      - /mnt/monitoring-data/loki:/loki
      - /home/ec2-user/loki-config.yml:/etc/loki/local-config.yaml
    command: -config.file=/etc/loki/local-config.yaml
    restart: unless-stopped
    networks:
      - monitoring

  tempo:
    image: grafana/tempo:latest
    container_name: tempo
    ports:
      - "4317:4317"  # OTLP gRPC
      - "4318:4318"  # OTLP HTTP
    volumes:
      - /mnt/monitoring-data/tempo:/var/tempo
      - /home/ec2-user/tempo-config.yml:/etc/tempo/config.yml
    command: -config.file=/etc/tempo/config.yml
    restart: unless-stopped
    networks:
      - monitoring

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    volumes:
      - /mnt/monitoring-data/grafana:/var/lib/grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    restart: unless-stopped
    depends_on:
      - prometheus
      - loki
      - tempo
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge
EOF

# Prometheus 설정 파일 생성
cat > /home/ec2-user/prometheus.yml <<'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Coffee Shout Application 추가
  # - job_name: 'coffee-shout-prod'
  #   metrics_path: '/actuator/prometheus'
  #   static_configs:
  #     - targets: ['<PROD_EC2_IP>:8080']

  # - job_name: 'coffee-shout-dev'
  #   metrics_path: '/actuator/prometheus'
  #   static_configs:
  #     - targets: ['<DEV_EC2_IP>:8080']
EOF

# Loki 설정 파일 생성
cat > /home/ec2-user/loki-config.yml <<'EOF'
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
    final_sleep: 0s
  chunk_idle_period: 5m
  chunk_retain_period: 30s

schema_config:
  configs:
    - from: 2023-01-01
      store: boltdb-shipper
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 24h

storage_config:
  boltdb_shipper:
    active_index_directory: /loki/index
    cache_location: /loki/cache
    shared_store: filesystem
  filesystem:
    directory: /loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h

chunk_store_config:
  max_look_back_period: 0s

table_manager:
  retention_deletes_enabled: true
  retention_period: 720h
EOF

# Tempo 설정 파일 생성
cat > /home/ec2-user/tempo-config.yml <<'EOF'
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317
        http:
          endpoint: 0.0.0.0:4318

storage:
  trace:
    backend: local
    local:
      path: /var/tempo/traces
    wal:
      path: /var/tempo/wal

compactor:
  compaction:
    block_retention: 720h
EOF

# 파일 소유권 변경
chown -R ec2-user:ec2-user /home/ec2-user/docker-compose.yml
chown -R ec2-user:ec2-user /home/ec2-user/prometheus.yml
chown -R ec2-user:ec2-user /home/ec2-user/loki-config.yml
chown -R ec2-user:ec2-user /home/ec2-user/tempo-config.yml

echo "Monitoring EC2 setup completed!" > /var/log/user-data.log
echo "Run 'docker-compose up -d' in /home/ec2-user to start monitoring stack" >> /var/log/user-data.log
