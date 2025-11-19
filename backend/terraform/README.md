# Coffee Shout Infrastructure - Terraform

## 📁 프로젝트 구조

```
terraform/
├── setup-backend.sh      # Backend 설정 스크립트 (S3 버킷, CORS, Lifecycle 등)
├── modules/
│   ├── vpc/              # VPC 모듈 (VPC, Subnets, IGW, Route Tables)
│   ├── security-group/   # Security Group 모듈
│   ├── ec2/              # EC2 모듈 (key pair 지원)
│   ├── rds/              # RDS 모듈 (프리티어 지원)
│   ├── elasticache/      # ElastiCache 모듈 (Valkey 지원)
│   └── s3/               # S3 모듈 (현재 외부 관리)
└── environments/
    ├── network/          # 네트워크 환경 (단일 VPC - Dev + Prod 공용)
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── outputs.tf
    │   └── terraform.tfvars (생성 필요)
    ├── dev/              # Dev 환경 (EC2, Security Group, Elastic IP)
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── outputs.tf
    │   ├── user-data.sh  # Java 21, Docker, Docker Compose 설치
    │   └── terraform.tfvars (생성 필요)
    └── prod/             # Prod 환경 (EC2, RDS, Valkey, Security Groups, Elastic IP)
        ├── main.tf
        ├── variables.tf
        ├── outputs.tf
        ├── user-data.sh  # Java 21만 설치
        └── terraform.tfvars (생성 필요)
```

## 🗂️ S3 버킷 구조

단일 S3 버킷 `coffee-shout`를 사용합니다 (외부 관리):

```
s3://coffee-shout/
├── terraform/tfstate/
│   ├── network/terraform.tfstate    # 네트워크 state (VPC, Subnets)
│   ├── dev/terraform.tfstate        # Dev 환경 state
│   └── prod/terraform.tfstate       # Prod 환경 state
└── qr/
    ├── dev/                         # Dev QR 코드 (30일 후 자동 삭제)
    └── prod/                        # Prod QR 코드 (30일 후 자동 삭제)
```

**S3 버킷 특징**:
- Object Lock으로 state locking (DynamoDB 불필요)
- 버저닝 활성화
- AES256 암호화
- QR 코드는 public read 허용
- Lifecycle rule로 QR 코드 30일 후 자동 삭제

## 🌐 네트워크 구성

### Network 환경
- **단일 VPC** (10.0.0.0/16) - Dev + Prod 공용
  - Public Subnets: 2개 (ap-northeast-2a, ap-northeast-2c)
    - 10.0.0.0/20 (ap-northeast-2a)
    - 10.0.16.0/20 (ap-northeast-2c)
  - Private Subnets: 2개 (RDS, ElastiCache용)
    - 10.0.32.0/20 (ap-northeast-2a)
    - 10.0.48.0/20 (ap-northeast-2c)
  - Internet Gateway 및 Route Tables 자동 구성

### Dev 환경 (개발/테스트)
**리소스**:
- **EC2**: t4g.small (ARM64), 20GB gp3, Elastic IP
- **Security Group**: SSH(22), HTTP(80), HTTPS(443), App(8080)
- **소프트웨어**: Java 21, Docker, Docker Compose
- **용도**: WAS + MySQL(Docker) + Redis(Docker) 통합 환경

**특징**:
- Docker로 DB와 캐시를 로컬에서 실행
- 빠른 개발/테스트 환경
- RDS/ElastiCache 비용 절감

### Prod 환경 (운영)
**리소스**:
- **EC2**: t4g.small (ARM64), 20GB gp3, Elastic IP
  - Security Group: SSH(22), HTTP(80), HTTPS(443), App(8080)
  - 소프트웨어: Java 21 (Docker 없음)
- **RDS MySQL**: db.t4g.micro (프리티어), 20GB gp3, Single-AZ
  - Engine: MySQL 8.0
  - Backup: 7일 보관
  - Security Group: EC2에서만 3306 접근
- **ElastiCache Valkey**: cache.t4g.micro (프리티어), 1 node
  - Engine: Valkey 7.2
  - Snapshot: 5일 보관
  - Security Group: EC2에서만 6379 접근
- **S3**: qr/prod/ (QR 코드 저장, 30일 후 자동 삭제)

**특징**:
- RDS/Valkey는 Private Subnet에 배포
- Managed Service로 안정성 확보
- Deletion protection 활성화 (실수 삭제 방지)

## 🚀 사용 방법

### 0. 사전 준비

```bash
# AWS CLI 설치 및 자격 증명 설정
aws configure

# AWS Key Pair 생성 (EC2 SSH 접속용)
# ap-northeast-2 리전에서 생성
# AWS Console → EC2 → Key Pairs → Create key pair
# .pem 파일 다운로드 및 안전하게 보관
```

### 1. Backend 설정 (최초 1회만)

S3 버킷 생성 및 설정:

```bash
cd terraform

# S3 버킷, CORS, Lifecycle, 암호화 등 자동 설정
./setup-backend.sh
```

**생성되는 리소스**:
- S3 버킷: `coffee-shout`
- Object Lock, 버저닝, 암호화 활성화
- CORS 설정 (QR 코드 접근용)
- Lifecycle rule (QR 코드 30일 후 삭제)

### 2. Network 환경 배포 (최우선)

**반드시 가장 먼저 Network를 배포해야 합니다!**

```bash
cd environments/network

# 초기화
terraform init

# 실행 계획 확인
terraform plan

# 배포
terraform apply

# 출력 확인 (VPC ID, Subnet ID 등)
terraform output
```

### 3. Dev 환경 배포

Network 배포 후 진행:

```bash
cd environments/dev

# terraform.tfvars 생성
cat > terraform.tfvars <<EOF
key_name = "your-key-pair-name"
EOF

# 초기화 및 배포
terraform init
terraform plan
terraform apply

# Elastic IP 확인
terraform output elastic_ip
```

**SSH 접속**:
```bash
ssh -i ~/.ssh/your-key.pem ec2-user@<elastic-ip>
```

### 4. Prod 환경 배포

Network 배포 후 진행:

```bash
cd environments/prod

# terraform.tfvars 생성
cat > terraform.tfvars <<EOF
key_name     = "your-key-pair-name"
db_password  = "your-secure-password-here"
EOF

# 초기화 및 배포
terraform init
terraform plan
terraform apply

# 출력 확인
terraform output
```

**중요 출력값**:
- `elastic_ip`: EC2 고정 IP
- `rds_endpoint`: MySQL 엔드포인트
- `valkey_endpoint`: Valkey 엔드포인트

## 🔗 환경 간 의존성

```
network (VPC, Subnet)
    ↓
    ├─→ dev (Dev 리소스)
    └─→ prod (Prod 리소스)
```

- **Network**: 독립적, 가장 먼저 배포
- **Dev/Prod**: Network의 출력값을 `terraform_remote_state`로 참조

## 📊 State 확인

```bash
# S3에 저장된 state 파일 목록
aws s3 ls s3://coffee-shout-tfstate/ --recursive

# 특정 환경의 state 확인
cd environments/network
terraform show

cd environments/dev
terraform show

cd environments/prod
terraform show
```

## 🗑️ 리소스 삭제

**삭제 순서가 중요합니다! (역순으로)**

```bash
# 1. Prod 환경 삭제
cd environments/prod
terraform destroy

# 2. Dev 환경 삭제
cd environments/dev
terraform destroy

# 3. Network 환경 삭제 (마지막)
cd environments/network
terraform destroy
```

## ⚠️ 주의사항

### 배포 및 삭제 순서
1. **배포 순서**: Backend (setup-backend.sh) → Network → Dev/Prod
2. **삭제 순서**: Prod → Dev → Network → Backend (수동 삭제)
3. Network를 먼저 삭제하면 Dev/Prod가 VPC를 참조하지 못해 오류 발생

### 보안
- **Key Pair**: AWS Console에서 생성, .pem 파일 안전하게 보관
- **DB Password**: 강력한 비밀번호 사용, terraform.tfvars는 .gitignore에 추가
- **Terraform State**: S3 버킷은 버저닝/암호화 활성화, 접근 제한
- **RDS/Valkey**: Private Subnet에 배포, EC2에서만 접근 가능

### 비용
- **프리티어 사양**:
  - EC2: t4g.small (프리티어 아님, 월 $15 예상)
  - RDS: db.t4g.micro (프리티어, 750시간/월 무료)
  - Valkey: cache.t4g.micro (프리티어, 750시간/월 무료)
- Dev 환경은 Docker로 RDS/Valkey 비용 절감
- 미사용 시 리소스 삭제 권장

### State 관리
- 각 환경의 state는 독립적으로 관리 (terraform/tfstate/{network,dev,prod}/)
- Remote state로 환경 간 데이터 공유 (VPC ID, Subnet ID 등)
- Object Lock으로 동시 수정 방지

## 📝 완료된 작업

1. ✅ VPC 모듈 생성
2. ✅ Security Group 모듈 생성
3. ✅ EC2 모듈 생성 (key pair 지원)
4. ✅ RDS 모듈 생성 (프리티어 지원)
5. ✅ ElastiCache 모듈 생성 (Valkey 지원)
6. ✅ Network 환경 구성 (단일 VPC, Dev + Prod 공용)
7. ✅ Dev 환경 구성 (EC2, Security Group, Elastic IP, Docker)
8. ✅ Prod 환경 구성 (EC2, RDS, Valkey, Security Groups, Elastic IP)
9. ✅ S3 Backend 구성 (Object Lock, CORS, Lifecycle)
10. ✅ S3 단일 버킷 구조 (terraform state + QR codes)

## 🔧 기술 스택

- **IaC**: Terraform
- **Cloud**: AWS
- **Compute**: EC2 (t4g.small, ARM64)
- **Database**: RDS MySQL 8.0 (db.t4g.micro)
- **Cache**: ElastiCache Valkey 7.2 (cache.t4g.micro)
- **Storage**: S3 (terraform state + QR codes)
- **Network**: VPC, Public/Private Subnets, Security Groups
- **Dev Tools**: Docker, Docker Compose (Dev only)
