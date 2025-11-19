#!/bin/bash
set -e

# 로그 파일
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

echo "Starting user data script..."

# 시스템 업데이트
apt-get update -y

# Java 21 설치 (Amazon Corretto)
wget -O- https://apt.corretto.aws/corretto.key | apt-key add -
add-apt-repository 'deb https://apt.corretto.aws stable main'
apt-get update -y
apt-get install -y java-21-amazon-corretto-jdk

# Docker 설치
apt-get install -y ca-certificates curl
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Docker 서비스 시작 및 자동 시작 설정
systemctl start docker
systemctl enable docker

# ubuntu 사용자를 docker 그룹에 추가
usermod -aG docker ubuntu

# CodeDeploy Agent 설치
apt-get install -y ruby-full wget
cd /home/ubuntu
wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
./install auto

# CodeDeploy Agent 시작 및 자동 시작 설정
systemctl start codedeploy-agent
systemctl enable codedeploy-agent

# AWS CLI 설치 (SSM Parameter Store 접근용)
apt-get install -y awscli jq

# SSM Parameter Store에서 환경 변수 가져오는 헬퍼 스크립트 생성
cat > /usr/local/bin/load-secrets.sh <<'SCRIPT'
#!/bin/bash
# SSM Parameter Store에서 환경 변수를 가져와 파일로 저장
PARAMETER_PATH="${parameter_path_prefix}"
REGION="ap-northeast-2"

# SSM Parameter Store에서 모든 파라미터 가져오기
mkdir -p /etc/environment.d

# 각 파라미터를 환경 변수 형식으로 저장
aws ssm get-parameters-by-path \
  --path "$PARAMETER_PATH" \
  --with-decryption \
  --region "$REGION" \
  --query 'Parameters[*].[Name,Value]' \
  --output text | while read -r name value; do
    # 파라미터 경로에서 마지막 부분만 추출 (예: /app/dev/DB_HOST -> DB_HOST)
    param_name=$(basename "$name" | tr '[:lower:]' '[:upper:]' | tr '-' '_')
    echo "$param_name=$value" >> /etc/environment.d/app-secrets.conf
done

# 환경 변수 파일을 /opt/coffee-shout/.env에도 복사 (애플리케이션용)
mkdir -p /opt/coffee-shout
cp /etc/environment.d/app-secrets.conf /opt/coffee-shout/.env

echo "Parameters loaded successfully from SSM Parameter Store!"
SCRIPT

chmod +x /usr/local/bin/load-secrets.sh

# 애플리케이션 디렉토리 생성 (be/prod 구조와 동일)
mkdir -p /opt/coffee-shout/app /opt/coffee-shout/scripts /opt/coffee-shout/logs
chown -R ubuntu:ubuntu /opt/coffee-shout

# 부팅 시 자동으로 Parameters 로드하도록 systemd 서비스 생성
cat > /etc/systemd/system/load-secrets.service <<'SERVICE'
[Unit]
Description=Load parameters from AWS SSM Parameter Store
After=network.target

[Service]
Type=oneshot
ExecStart=/usr/local/bin/load-secrets.sh
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
SERVICE

systemctl daemon-reload
systemctl enable load-secrets.service
systemctl start load-secrets.service

echo "User data script completed successfully!"
