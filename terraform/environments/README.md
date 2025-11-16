# Terraform 환경별 설정 가이드

## 최근 변경사항 (2024-11-16)

### ✨ 주요 개선
- ✅ **모듈 정리**: 사용하지 않는 compute, database 모듈 삭제
- ✅ **변수 통일**: RDS 모듈 변수/output 이름 일관성 개선
- ✅ **비용 절감**: RDS CloudWatch Logs에서 general 로그 제거 (프리티어 초과 방지)
- ✅ **구조 간소화**: terraform 루트 디렉토리 초기 설정 파일 제거
- ✅ **네이밍 일관성**: ElastiCache, RDS 모듈 output 이름 통일

### 🎯 현재 모듈 구성 (9개)
1. **network** - VPC, Subnet, IGW, Route Table
2. **security-groups** - 계층별 보안 그룹 (ALB, EC2, RDS, ElastiCache)
3. **ec2** - Ubuntu 24.04 ARM64 백엔드 서버
4. **alb** - Application Load Balancer
5. **rds** - MySQL 8.0 (Private Subnet)
6. **elasticache** - Valkey 8.0 (Private Subnet)
7. **s3** - S3 버킷 (자동 이름 생성)
8. **iam** - IAM Role 및 정책
9. **secrets** - Secrets Manager

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

### PROD 환경 (ACM 인증서 필요)

```bash
# 1. ACM 인증서 생성 (AWS Console에서)
# 2. ARN 복사

# 3. PROD 환경 배포
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # certificate_arn 설정

# 4. 실행
terraform init
terraform plan
terraform apply

# 5. 결과 확인
terraform output alb_dns_name  # HTTPS로 접속
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
├── modules/                 # 재사용 가능한 모듈 (9개)
│   ├── network/            # VPC, Subnet, IGW, Route Table
│   ├── security-groups/    # Security Groups (ALB, EC2, RDS, ElastiCache)
│   ├── ec2/                # EC2 인스턴스 (Ubuntu 24.04 ARM64)
│   ├── alb/                # Application Load Balancer
│   ├── rds/                # RDS MySQL 8.0
│   ├── elasticache/        # ElastiCache Valkey 8.0
│   ├── s3/                 # S3 버킷 (자동 이름 생성)
│   ├── iam/                # IAM 역할 및 정책
│   └── secrets/            # Secrets Manager (환경변수 통합 관리)
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
- ✅ 모듈 재사용: 9개 모듈로 구성
- ✅ 백엔드 분리: 각 환경별 S3 state 파일

---

## 주요 특징

### 자동 생성 기능
- **S3 버킷 이름**: `{project_name}-{environment}-bucket` 형식으로 자동 생성
  - DEV: `coffeeshout-dev-bucket`
  - PROD: `coffeeshout-prod-bucket`
- **RDS 비밀번호**: Terraform의 `random_password` 리소스로 자동 생성 후 Secrets Manager에 저장

### 네트워크 설계
- **Public Subnet**: ALB, EC2 배치 (인터넷 접근 가능)
- **Private Subnet**: RDS, ElastiCache 배치 (인터넷 차단, VPC 내부만)
- **NAT Gateway 미사용**: 비용 절감 (~$35/월)
  - RDS/ElastiCache는 인터넷 접근 불필요
  - VPC 내부 통신만 사용

### 보안
- **계층별 Security Group 분리**: ALB → EC2 → RDS/ElastiCache
- **최소 권한 원칙**: 필요한 포트만 오픈
- **Private Subnet 격리**: 데이터베이스는 인터넷에서 완전 차단
- **암호화**: S3, RDS, EBS 모두 암호화 활성화

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

### 3.1 변수 파일 설정

```bash
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # 실제 값으로 수정
```

**필수 수정 항목:**
- `certificate_arn`: ACM Certificate ARN (HTTPS용)

### 3.2 Terraform 검증 및 실행

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

### 3.3 배포 완료 후 확인

```bash
# 출력값 확인
terraform output

# RDS 엔드포인트
terraform output rds_endpoint

# ElastiCache 엔드포인트
terraform output elasticache_endpoint

# ALB DNS
terraform output alb_dns_name
```

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
- ⚠️ ElastiCache: 750시간/월 초과 시 ~$11/월

**비용 발생 항목:**
- ElastiCache: DEV + PROD 동시 사용 시 월 690시간 초과 (~$11/월)
- Elastic IP: 인스턴스 중지 시 과금 ($3.6/월)

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
