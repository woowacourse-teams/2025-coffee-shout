# Terraform 환경별 설정 가이드

## 🔥 최신 변경사항 (2025-11-20) - Phase 7: 인프라 재설계

### ⚠️ 중요: 아키텍처 대대적 변경

AWS 프리티어 정책 변경 및 비용 최적화를 위해 인프라를 **완전히 재설계**했습니다.

### 🎯 Phase 7 변경사항: Docker 기반 인프라로 전환

#### ❌ 제거된 AWS 관리형 서비스
- **RDS** → Docker MySQL로 대체
- **ElastiCache** → Docker Valkey로 대체
- **CodeBuild + CodeDeploy + CodePipeline** → GitHub Actions로 대체

#### ✨ 새로운 아키텍처

```
3개 EC2 서버 구성:

1️⃣ DEV 서버 (t4g.small)
   - WAS (Spring Boot)
   - Docker MySQL 8.0
   - Docker Valkey 8.0

2️⃣ PROD 서버 (t4g.small)
   - WAS (Spring Boot)
   - Docker MySQL 8.0
   - Docker Valkey 8.0

3️⃣ Monitoring 서버 (t4g.small)
   - Grafana
   - Prometheus
   - Tempo
```

#### 💰 비용 효과

**현재 (Phase 6):**
- ElastiCache 2대: ~$11/월
- 기타: $0 (프리티어)
- **총: ~$11/월**

**새 아키텍처 (Phase 7):**
- EC2 3대: $0 (2025년 12월까지 t4g 무료)
- Docker 서비스: $0
- **총: $0/월** ✨

**2026년 1월 이후:**
- EC2 3대: ~$21/월
- **절감: 관리형 서비스 비용 제거, 완전한 제어**

#### 📦 새로 추가된 파일

- `.github/workflows/deploy-dev.yml` - DEV 배포 워크플로우
- `.github/workflows/deploy-prod.yml` - PROD 배포 워크플로우
- `terraform/docker/dev-docker-compose.yml` - DEV 환경 Docker Compose
- `terraform/docker/prod-docker-compose.yml` - PROD 환경 Docker Compose
- `terraform/docker/monitoring-docker-compose.yml` - 모니터링 스택
- `scripts/deploy.sh` - 배포 스크립트
- `scripts/backup.sh` - 백업 스크립트
- `scripts/healthcheck.sh` - 헬스체크 스크립트

#### 📖 마이그레이션 가이드

상세한 마이그레이션 가이드는 아래 문서를 참고하세요:
- [Docker Compose 가이드](../docker/README.md)
- [배포 스크립트 가이드](../../scripts/README.md)

#### ⚠️ 주의사항

- **데이터 마이그레이션 필수**: RDS → Docker MySQL 데이터 이관 필요
- **백업 전략 변경**: 자동 백업 → 수동 백업 스크립트 (`scripts/backup.sh`)
- **모니터링 변경**: CloudWatch RDS Insights → Prometheus + Grafana

---

## 이전 변경사항 (2025-11-16)

### ✨ Phase 6 완료: 인프라 보안 및 비용 최적화
- ✅ **IAM 권한 최소화**: CodeBuild SNS Publish 권한을 특정 Topic ARN으로 제한
- ✅ **Secrets Manager 제거**: RDS 비밀번호를 SSM Parameter Store로 완전 통합 (월 $0.40 절감)
- ✅ **EC2 User Data 정리**: 사용하지 않는 Secrets Manager 코드 제거 (복잡도 감소)

### ✨ Phase 5 완료: Backend 배포 파일 통합
- ✅ **Backend 배포 파일**: origin/be/prod에서 buildspec, appspec, scripts 병합
- ✅ **Profile별 설정**: application-prod.yml, application-dev.yml, application-local.yml, application-test.yml 추가
- ✅ **환경변수 통합**: Redis 설정을 환경변수로 변경 (${REDIS_HOST}, ${REDIS_PORT})

### ✨ Phase 4 완료: CI/CD Pipeline
- ✅ **CodeBuild**: Java 21 빌드, SSM 환경변수 자동 주입, SNS 빌드 실패 알림
- ✅ **CodeDeploy**: EC2 무중단 배포, Graceful Shutdown
- ✅ **CodePipeline**: GitHub → Build → Deploy 자동화 (무료 티어 1개)

### ✨ Phase 3 완료: 모니터링 및 알림
- ✅ **SSM Parameter Store**: 환경변수 중앙 관리 (MySQL, Redis, S3, Tempo 등)
- ✅ **Lambda + SNS**: Slack 알림 자동화 (빌드 실패, 배포 실패)
- ✅ **완전 무료**: Lambda, SNS, SSM 모두 프리티어 내 무료

### ✨ Phase 2 완료: CloudWatch 모니터링
- ✅ **CloudWatch Alarms**: CPU, 메모리, 디스크 사용률 모니터링
- ✅ **비용 절감**: RDS CloudWatch Logs에서 general 로그 제거 (프리티어 초과 방지)

### 🎯 현재 모듈 구성 (15개)
1. **network** - VPC, Subnet, IGW, Route Table
2. **security-groups** - 계층별 보안 그룹 (ALB, EC2, RDS, ElastiCache)
3. **ec2** - Ubuntu 24.04 ARM64 백엔드 서버
4. **alb** - Application Load Balancer
5. **rds** - MySQL 8.0 (Private Subnet, 비밀번호 자동 생성)
6. **elasticache** - Valkey 8.0 (Private Subnet)
7. **s3** - S3 버킷 (자동 이름 생성) + CodePipeline Artifacts
8. **iam** - IAM Role 및 정책 (EC2, CodeBuild, CodeDeploy, CodePipeline, Lambda)
9. **secrets** - SSM Parameter Store (환경변수 중앙 관리, 완전 무료)
10. **sns** - SNS Topic (Slack 알림용)
11. **lambda** - Lambda Function (SNS → Slack 메시지 전송)
12. **monitoring** - CloudWatch Alarms (CPU, 메모리, 디스크)
13. **codebuild** - CodeBuild Project (Java 21, SSM 환경변수 자동 주입)
14. **codedeploy** - CodeDeploy (EC2 무중단 배포)
15. **codepipeline** - CodePipeline (GitHub → Build → Deploy)

---

## 🚀 빠른 시작

### DEV 환경 (5분 안에 시작)

```bash
# 1. Bootstrap (최초 1회만)
cd terraform/bootstrap
terraform init && terraform apply

# 2. DEV 환경 배포
cd ../environments/dev
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # mysql_password 설정

# 3. 실행
terraform init
terraform plan    # 미리보기
terraform apply   # 실제 생성 (yes 입력)

# 4. 결과 확인
terraform output
```

### PROD 환경 (ACM 인증서 + GitHub Connection 필요)

```bash
# 1. ACM 인증서 생성 (AWS Console에서)
# 2. GitHub CodeStar Connection 생성 (AWS Console에서)
#    - Developer Tools → CodePipeline → Settings → Connections
#    - "Create connection" → Provider: GitHub → 인증 완료
# 3. SSM Parameter Store에 환경변수 등록
#    - /coffee-shout/prod/mysql-url
#    - /coffee-shout/prod/mysql-username
#    - /coffee-shout/prod/mysql-password
#    - /coffee-shout/prod/redis-host
#    - /coffee-shout/prod/redis-port
#    - /coffee-shout/prod/s3-bucket-name
#    - /coffee-shout/prod/s3-qr-key-prefix
#    - /coffee-shout/prod/tempo-url
#    - /coffee-shout/prod/trace-sampling-probability

# 4. PROD 환경 배포
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # certificate_arn, github_connection_arn 설정

# 5. 실행
terraform init
terraform plan
terraform apply

# 6. 결과 확인
terraform output alb_dns_name  # HTTPS로 접속
terraform output codepipeline_name  # CI/CD 파이프라인 확인
```

---

## 환경 구성

### 네트워크 아키텍처 (AWS Best Practice)

```
VPC (10.0.0.0/16 for DEV, 10.1.0.0/16 for PROD)
│
├─ Public Subnet (인터넷 접근 가능)
│  ├─ ALB (80/443)
│  └─ EC2 (8080)
│
└─ Private Subnet (인터넷 차단, VPC 내부만 통신)
   ├─ RDS (3306) ← EC2에서만 접근 가능
   └─ ElastiCache (6379) ← EC2에서만 접근 가능
```

**보안:**
- RDS/ElastiCache는 Private Subnet에 격리
- Security Group으로 EC2에서만 접근 가능
- NAT Gateway 불필요 (RDS/ElastiCache는 인터넷 접근 안함)

### DEV 환경
- **VPC**: 10.0.0.0/16
- **Public Subnet**: 10.0.1.0/24, 10.0.2.0/24
- **Private Subnet**: 10.0.10.0/24, 10.0.11.0/24
- **EC2**: t4g.small (Public Subnet)
- **MySQL**: Docker 컨테이너 (localhost:3306)
- **ElastiCache**: Valkey 8.0 (cache.t3.micro, Private Subnet)
- **S3**: 자동 생성 버킷 (`coffeeshout-dev-bucket`)
- **ALB**: HTTP만 (Public Subnet)
- **비용**: ElastiCache 프리티어 초과 시 ~$11/월

### PROD 환경
- **VPC**: 10.1.0.0/16
- **Public Subnet**: 10.1.1.0/24, 10.1.2.0/24
- **Private Subnet**: 10.1.10.0/24, 10.1.11.0/24
- **EC2**: t4g.small + Elastic IP (Public Subnet)
- **RDS**: MySQL 8.0.43 (db.t3.micro, Private Subnet)
- **ElastiCache**: Valkey 8.0 (cache.t3.micro, Private Subnet)
- **S3**: 자동 생성 버킷 (`coffeeshout-prod-bucket`)
- **ALB**: HTTPS (ACM 인증서 필요, Public Subnet)
- **비용**: ElastiCache 프리티어 초과 시 ~$11/월

**참고**: ElastiCache 프리티어는 월 750시간이므로, DEV + PROD 두 개 사용 시 약 690시간 초과됩니다.

---

## 모듈 구조

```
terraform/
├── bootstrap/               # S3/DynamoDB 백엔드 초기화
│   ├── main.tf
│   ├── variables.tf
│   └── outputs.tf
├── modules/                 # 재사용 가능한 모듈 (15개)
│   ├── network/            # VPC, Subnet, IGW, Route Table
│   ├── security-groups/    # Security Groups (ALB, EC2, RDS, ElastiCache)
│   ├── ec2/                # EC2 인스턴스 (Ubuntu 24.04 ARM64)
│   ├── alb/                # Application Load Balancer
│   ├── rds/                # RDS MySQL 8.0 (비밀번호 자동 생성)
│   ├── elasticache/        # ElastiCache Valkey 8.0
│   ├── s3/                 # S3 버킷 (자동 이름 생성) + CodePipeline Artifacts
│   ├── iam/                # IAM 역할 및 정책 (최소 권한 원칙)
│   ├── secrets/            # SSM Parameter Store (환경변수 중앙 관리, 무료)
│   ├── sns/                # SNS Topic (Slack 알림용)
│   ├── lambda/             # Lambda Function (SNS → Slack 메시지 전송)
│   ├── monitoring/         # CloudWatch Alarms (CPU, 메모리, 디스크)
│   ├── codebuild/          # CodeBuild Project (Java 21, SSM 환경변수 자동 주입)
│   ├── codedeploy/         # CodeDeploy (EC2 무중단 배포)
│   └── codepipeline/       # CodePipeline (GitHub → Build → Deploy)
└── environments/           # 환경별 설정 (실제 사용)
    ├── dev/                # DEV 환경
    │   ├── main.tf         # 모듈 조합
    │   ├── variables.tf    # 변수 정의
    │   ├── outputs.tf      # 출력값
    │   ├── backend.tf      # S3 백엔드 설정
    │   ├── provider.tf     # AWS Provider
    │   ├── terraform.tfvars.example
    │   └── docker-compose.yml
    └── prod/               # PROD 환경
        ├── main.tf
        ├── variables.tf
        ├── outputs.tf
        ├── backend.tf
        ├── provider.tf
        └── terraform.tfvars.example
```

**주요 특징:**
- ✅ 환경별 격리: DEV/PROD 완전 분리
- ✅ 모듈 재사용: 15개 모듈로 구성
- ✅ 백엔드 분리: 각 환경별 S3 state 파일
- ✅ CI/CD 자동화: CodePipeline으로 GitHub → Build → Deploy 자동화
- ✅ 환경변수 관리: SSM Parameter Store로 중앙 관리

---

## 주요 특징

### 자동 생성 기능
- **S3 버킷 이름**: `{project_name}-{environment}-bucket` 형식으로 자동 생성
  - DEV: `coffeeshout-dev-bucket`
  - PROD: `coffeeshout-prod-bucket`
- **RDS 비밀번호**: Terraform의 `random_password` 리소스로 자동 생성 후 SSM Parameter Store에 저장 (무료)

### 네트워크 설계
- **Public Subnet**: ALB, EC2 배치 (인터넷 접근 가능)
- **Private Subnet**: RDS, ElastiCache 배치 (인터넷 차단, VPC 내부만)
- **NAT Gateway 미사용**: 비용 절감 (~$35/월)
  - RDS/ElastiCache는 인터넷 접근 불필요
  - VPC 내부 통신만 사용

### 보안
- **계층별 Security Group 분리**: ALB → EC2 → RDS/ElastiCache
- **IAM 권한 최소화**: 각 서비스별 필요한 리소스에만 접근 가능
- **최소 권한 원칙**: 필요한 포트만 오픈
- **Private Subnet 격리**: 데이터베이스는 인터넷에서 완전 차단
- **암호화**: S3, RDS, EBS 모두 암호화 활성화
- **SSM Parameter Store**: 민감 정보를 SecureString으로 암호화 저장

---

## 1. 사전 준비

### 1.1 필수 도구 설치

```bash
# Terraform 설치
brew install terraform  # macOS
# 또는
wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip

# AWS CLI 설치
brew install awscli  # macOS
# 또는
pip install awscli

# AWS 자격증명 설정
aws configure
```

### 1.2 Terraform 백엔드 초기화

**중요**: 가장 먼저 실행해야 합니다!

S3와 DynamoDB를 생성하여 Terraform state 파일을 관리합니다:

```bash
cd terraform/bootstrap
terraform init
terraform apply
```

생성되는 리소스:
- S3 버킷: `coffeeshout-terraform-state-dev`, `coffeeshout-terraform-state-prod`
- DynamoDB 테이블: `coffeeshout-terraform-lock-dev`, `coffeeshout-terraform-lock-prod`

### 1.3 ACM 인증서 생성 (PROD만)

PROD 환경에서 HTTPS를 사용하려면 ACM 인증서가 필요합니다:

1. AWS Console → Certificate Manager (ap-northeast-2 리전)
2. 인증서 요청 → 공개 인증서 요청
3. 도메인 이름 입력 (예: `*.coffeeshout.com`)
4. DNS 또는 이메일 검증 완료
5. 생성된 ARN을 복사 → `terraform.tfvars`의 `certificate_arn`에 입력

**예시 ARN:**
```
arn:aws:acm:ap-northeast-2:123456789012:certificate/12345678-1234-1234-1234-123456789012
```

---

## 2. DEV 환경 배포

### 2.1 변수 파일 설정

```bash
cd terraform/environments/dev
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # 실제 값으로 수정
```

**필수 수정 항목:**
- `mysql_password`: MySQL 비밀번호 설정

### 2.2 Terraform 검증 및 실행

```bash
# 1. 코드 포맷 확인 및 자동 수정
terraform fmt -recursive

# 2. 문법 검증
terraform validate

# 3. 초기화
terraform init

# 4. 실행 계획 확인 (변경사항 미리보기)
terraform plan

# 5. 인프라 생성
terraform apply
```

**검증 체크리스트:**
- ✅ `terraform fmt`: 코드 포맷팅 정상
- ✅ `terraform validate`: 문법 오류 없음
- ✅ `terraform init`: Provider 플러그인 설치 완료
- ✅ `terraform plan`: 생성될 리소스 확인 (오류 없음)

### 2.3 Docker Compose 파일 배포

EC2 인스턴스에 SSH 접속 후:

```bash
# Docker Compose 파일 복사
sudo cp terraform/environments/dev/docker-compose.yml /opt/coffee-shout/

# 환경 변수 설정 (선택)
cd /opt/coffee-shout
cat > .env.docker <<EOF
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=coffeeshout_dev
MYSQL_USERNAME=coffeeshout
MYSQL_PASSWORD=your-password
EOF

# MySQL 컨테이너 시작
docker compose up -d

# 상태 확인
docker compose ps
docker compose logs -f
```

### 2.4 컨테이너 관리

```bash
# 시작
docker compose up -d

# 중지
docker compose down

# 재시작
docker compose restart

# 로그 확인
docker compose logs -f mysql
```

### 2.5 ElastiCache 엔드포인트 확인

```bash
# Terraform으로 ElastiCache 엔드포인트 확인
terraform output elasticache_endpoint
terraform output elasticache_host
```

---

## 3. PROD 환경 배포

### 3.1 사전 준비 (AWS Console)

**1. GitHub CodeStar Connection 생성**
```bash
# AWS Console → Developer Tools → CodePipeline → Settings → Connections
# 1. "Create connection" 클릭
# 2. Provider: GitHub 선택
# 3. Connection name: coffee-shout-github
# 4. GitHub 인증 완료 후 ARN 복사
```

**2. SSM Parameter Store 환경변수 등록**
```bash
# MySQL 설정
aws ssm put-parameter --name "/coffee-shout/prod/mysql-url" \
  --value "jdbc:mysql://RDS_ENDPOINT:3306/coffee_shout?characterEncoding=UTF-8&serverTimezone=Asia/Seoul" \
  --type "SecureString" --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/mysql-username" \
  --value "admin" \
  --type "SecureString" --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/mysql-password" \
  --value "YOUR_MYSQL_PASSWORD" \
  --type "SecureString" --region ap-northeast-2

# Redis 설정
aws ssm put-parameter --name "/coffee-shout/prod/redis-host" \
  --value "ELASTICACHE_ENDPOINT" \
  --type "SecureString" --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/redis-port" \
  --value "6379" \
  --type "String" --region ap-northeast-2

# S3 설정
aws ssm put-parameter --name "/coffee-shout/prod/s3-bucket-name" \
  --value "coffee-shout-prod-bucket" \
  --type "String" --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/s3-qr-key-prefix" \
  --value "qr/" \
  --type "String" --region ap-northeast-2

# Tempo 설정
aws ssm put-parameter --name "/coffee-shout/prod/tempo-url" \
  --value "http://TEMPO_ENDPOINT:4318/v1/traces" \
  --type "String" --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/trace-sampling-probability" \
  --value "0.1" \
  --type "String" --region ap-northeast-2
```

**참고**: RDS/ElastiCache 엔드포인트는 Terraform apply 후 `terraform output`으로 확인 가능

### 3.2 변수 파일 설정

```bash
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # 실제 값으로 수정
```

**필수 수정 항목:**
- `certificate_arn`: ACM Certificate ARN (HTTPS용)
- `github_connection_arn`: GitHub CodeStar Connection ARN
- `github_repo`: "woowacourse-teams/2025-coffee-shout"
- `github_branch`: 배포할 브랜치 (예: "main" 또는 "be/prod")

### 3.3 Terraform 검증 및 실행

```bash
# 1. 코드 포맷 확인 및 자동 수정
terraform fmt -recursive

# 2. 문법 검증
terraform validate

# 3. 초기화
terraform init

# 4. 실행 계획 확인 (변경사항 미리보기)
terraform plan

# 5. 인프라 생성
terraform apply
```

**검증 체크리스트:**
- ✅ `terraform fmt`: 코드 포맷팅 정상
- ✅ `terraform validate`: 문법 오류 없음
- ✅ `terraform init`: Provider 플러그인 설치 완료
- ✅ `terraform plan`: 생성될 리소스 확인 (오류 없음)

### 3.4 배포 완료 후 확인

```bash
# 출력값 확인
terraform output

# RDS 엔드포인트 (SSM Parameter Store 업데이트 필요)
terraform output rds_endpoint

# ElastiCache 엔드포인트 (SSM Parameter Store 업데이트 필요)
terraform output elasticache_endpoint

# ALB DNS (Route 53에 등록)
terraform output alb_dns_name

# CodePipeline 확인
terraform output codepipeline_name
terraform output codepipeline_url
```

### 3.5 배포 후 SSM 파라미터 업데이트

Terraform apply 후 RDS/ElastiCache 엔드포인트를 확인하여 SSM에 업데이트:

```bash
# RDS 엔드포인트 확인
RDS_ENDPOINT=$(terraform output -raw rds_endpoint | cut -d: -f1)

# ElastiCache 엔드포인트 확인
REDIS_HOST=$(terraform output -raw elasticache_host)

# SSM 파라미터 업데이트
aws ssm put-parameter --name "/coffee-shout/prod/mysql-url" \
  --value "jdbc:mysql://${RDS_ENDPOINT}:3306/coffee_shout?characterEncoding=UTF-8&serverTimezone=Asia/Seoul" \
  --type "SecureString" --overwrite --region ap-northeast-2

aws ssm put-parameter --name "/coffee-shout/prod/redis-host" \
  --value "${REDIS_HOST}" \
  --type "SecureString" --overwrite --region ap-northeast-2
```

### 3.6 CI/CD 파이프라인 실행

GitHub에 코드를 푸시하면 자동으로 CodePipeline이 실행됩니다:

```bash
# GitHub 푸시 → CodePipeline 자동 실행
git push origin main

# AWS Console에서 파이프라인 진행 상황 확인:
# Developer Tools → CodePipeline → coffee-shout-prod-pipeline
```

**파이프라인 단계:**
1. **Source**: GitHub에서 코드 가져오기
2. **Build**: CodeBuild로 Gradle 빌드 (Java 21, envsubst 환경변수 치환)
3. **Deploy**: CodeDeploy로 EC2에 무중단 배포

---

## 4. 인프라 관리

### 4.1 변경사항 적용

```bash
# 변경사항 미리보기
terraform plan

# 적용
terraform apply
```

### 4.2 특정 리소스만 변경

```bash
# EC2만 재생성
terraform apply -target=module.ec2

# RDS만 변경
terraform apply -target=module.rds
```

### 4.3 상태 확인

```bash
# 전체 리소스 확인
terraform state list

# 특정 리소스 상세 정보
terraform state show module.ec2.aws_instance.backend
```

### 4.4 인프라 삭제

```bash
# 주의: 모든 리소스가 삭제됩니다!
terraform destroy
```

---

## 5. 문제 해결

### 5.1 Terraform 초기화 오류

```bash
# 백엔드 재설정
terraform init -reconfigure

# 플러그인 재다운로드
terraform init -upgrade
```

### 5.2 RDS 비밀번호 확인 (PROD)

```bash
# Secrets Manager에서 확인
aws secretsmanager get-secret-value \
  --secret-id coffeeshout-prod-secrets \
  --region ap-northeast-2 \
  --query SecretString \
  --output text | jq -r '.MYSQL_PASSWORD'
```

### 5.3 Docker 컨테이너 문제 (DEV)

```bash
# 컨테이너 로그 확인
docker compose logs mysql
docker compose logs redis

# 컨테이너 재시작
docker compose restart mysql

# 완전 재생성
docker compose down -v
docker compose up -d
```

---

## 6. 비용 최적화 팁

### 6.1 프리티어 유지 전략

**완전 무료 유지 (월 $0):**
- ✅ EC2: t4g.small (2025년 12월까지 무료)
- ✅ RDS: db.t3.micro 750시간/월 + 20GB 스토리지
- ✅ RDS 백업: allocated_storage만큼 무료 (20GB)
- ✅ CloudWatch Logs: 5GB 수집/저장 무료 (general 로그 제거로 프리티어 내 유지)
- ✅ S3: 5GB 스토리지 + 20,000 GET/2,000 PUT
- ✅ CodeBuild: 월 100분 무료 (build 시간만 과금)
- ✅ CodeDeploy: EC2 배포 완전 무료
- ✅ CodePipeline: 월 1개 파이프라인 무료
- ✅ Lambda: 월 100만 요청 + 40만 GB-초 무료 (Slack 알림)
- ✅ SNS: 월 1,000건 이메일 발행 무료
- ✅ SSM Parameter Store: Standard 파라미터 무료
- ⚠️ ElastiCache: 750시간/월 초과 시 ~$11/월

**비용 발생 항목:**
- ElastiCache: DEV + PROD 동시 사용 시 월 690시간 초과 (~$11/월)
- Elastic IP: 인스턴스 중지 시 과금 ($3.6/월)
- CodeBuild: 월 100분 초과 시 ($0.005/분)
- CodePipeline: 2개 이상 파이프라인 사용 시 ($1/월)

**Phase 6 개선으로 절감된 비용:**
- ✅ Secrets Manager 제거: **월 $0.40 절감** (SSM Parameter Store로 대체)

### 6.2 일별/주별 절약 팁

1. **DEV 환경 중지 (사용하지 않을 때)**
   ```bash
   # EC2 중지 (EIP 과금 주의!)
   aws ec2 stop-instances --instance-ids i-xxxxx

   # Docker 컨테이너 중지
   docker compose down
   ```

2. **ElastiCache 최적화**
   - **옵션 A**: DEV에서 Docker Redis 사용 → 완전 무료
   - **옵션 B**: DEV ElastiCache 제거 → PROD만 사용 시 100% 프리티어

3. **RDS 백업 최적화**
   - PROD: 7일 보관 (권장) - 백업 스토리지 20GB 내 유지 가능
   - DEV: 백업 비활성화 고려 (복구 불가능하지만 비용 절감)

4. **CloudWatch Logs 자동 정리**
   - DEV: 7일 자동 삭제
   - PROD: 30일 자동 삭제
   - RDS: error + slowquery만 (general 제거로 비용 절감)

5. **S3 Lifecycle 정책** (자동 적용)
   - 90일 경과: Standard-IA로 이동
   - 180일 경과: Glacier로 이동
   - 365일 경과: 자동 삭제

---

## 7. 보안 주의사항

1. ✅ `terraform.tfvars`는 절대 Git에 커밋하지 마세요 (.gitignore 적용됨)
2. ✅ AWS Access Key는 환경 변수 또는 AWS CLI 설정 사용
3. ✅ RDS 비밀번호는 Secrets Manager에서 자동 생성
4. ✅ 모든 리소스는 암호화 활성화
5. ✅ Security Group은 최소 권한 원칙 적용

---

## 8. 추가 리소스

- [Terraform 공식 문서](https://www.terraform.io/docs)
- [AWS 프리티어 안내](https://aws.amazon.com/free/)
- [Docker Compose 문서](https://docs.docker.com/compose/)
