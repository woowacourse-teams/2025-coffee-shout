# Coffee Shout 인프라 Terraform 구축 체크리스트

> **✅ Terraform 코드 구축 완료!**
> - 5개 재사용 가능한 모듈 구현 (VPC, Security Group, EC2, RDS, ElastiCache)
> - 3개 환경 (network, dev, prod) 설정 완료
> - S3 Backend 구성 (Object Lock, CORS, Lifecycle)
> - 상세 가이드: [terraform/README.md](../terraform/README.md)

## 개요
Coffee Shout 프로젝트의 인프라를 Terraform으로 구축합니다.

### 목표 인프라 구성
- **Network 환경**: 단일 VPC (Dev + Prod 공용), Public/Private Subnets
- **Production 환경**: WAS EC2 1대 (t4g.small), ElastiCache Valkey 1대, RDS MySQL 1대
- **Development 환경**: EC2 1대 (t4g.small) - WAS + Docker (MySQL + Redis)

### 인스턴스 타입
- **EC2**: t4g.small (ARM 기반 Graviton2, 2 vCPU, 2GB RAM)
- **RDS**: db.t4g.micro (프리티어, ARM 기반)
- **ElastiCache**: cache.t4g.micro (프리티어, ARM 기반)

---

## 1. Terraform 프로젝트 구조

### 1.1 디렉토리 생성 ✅
```
terraform/
├── setup-backend.sh      # S3 Backend 설정 스크립트
├── environments/
│   ├── network/         # VPC, Subnets (Dev + Prod 공용)
│   ├── dev/            # Dev 환경
│   └── prod/           # Prod 환경
└── modules/
    ├── vpc/            # VPC 모듈
    ├── security-group/ # Security Group 모듈
    ├── ec2/           # EC2 모듈
    ├── rds/           # RDS 모듈
    ├── elasticache/   # ElastiCache 모듈 (Valkey 지원)
    └── s3/            # S3 모듈 (현재 외부 관리)
```

### 1.2 Terraform Backend 설정 ✅
- [x] S3 버킷 생성 (`coffee-shout`)
- [x] Object Lock 활성화 (DynamoDB 대체)
- [x] 버저닝 및 암호화 활성화
- [x] CORS 설정 (QR 코드 접근용)
- [x] Lifecycle rule (QR 코드 30일 후 삭제)
- [x] Backend 구성 (terraform/tfstate/{network,dev,prod}/)

---

## 2. 네트워크 구성

### 2.1 VPC 모듈 ✅
- [x] VPC CIDR 블록 설정 (10.0.0.0/16)
- [x] Public Subnet 생성 2개 (ap-northeast-2a, 2c)
- [x] Private Subnet 생성 2개 (RDS, ElastiCache용)
- [x] Internet Gateway 생성
- [x] Route Table 구성 (Public/Private)

### 2.2 Network 환경 ✅
- [x] Network 환경 구성 (단일 VPC)
- [x] Remote state 출력 (VPC ID, Subnet IDs)

---

## 3. 보안 그룹

### 3.1 Production 보안 그룹 ✅
- [x] WAS EC2 SG: 22, 80, 443, 8080
- [x] RDS SG: 3306 (EC2에서만 허용)
- [x] ElastiCache SG: 6379 (EC2에서만 허용)

### 3.2 Development 보안 그룹 ✅
- [x] Dev EC2 SG: 22, 80, 443, 8080

---

## 4. EC2 인스턴스

### 4.1 EC2 모듈 ✅
- [x] ARM64 AMI 자동 선택 (Amazon Linux 2023)
- [x] Key pair 지원
- [x] User data 지원
- [x] Root volume 설정 (크기, 타입, 암호화)
- [x] Security group 연결
- [x] IAM instance profile 지원

### 4.2 Production WAS EC2 (t4g.small) ✅
- [x] ARM64 AMI (Amazon Linux 2023)
- [x] User Data 스크립트: Java 21 설치
- [x] Elastic IP 할당
- [x] Security Group 설정
- [x] Key pair 설정 (SSH 접속용)

### 4.3 Development EC2 (t4g.small) ✅
- [x] ARM64 AMI (Amazon Linux 2023)
- [x] User Data 스크립트
  - [x] Java 21 설치
  - [x] Docker 설치
  - [x] Docker Compose 설치
- [x] Elastic IP 할당
- [x] Security Group 설정
- [x] Key pair 설정

---

## 5. RDS (MySQL)

### 5.1 RDS 모듈 ✅
- [x] MySQL/PostgreSQL/MariaDB 지원
- [x] 프리티어 사양 지원 (db.t4g.micro)
- [x] DB Subnet Group 자동 생성
- [x] 암호화, 백업, Multi-AZ 설정
- [x] Deletion protection 지원

### 5.2 Production RDS ✅
- [x] MySQL 8.0 엔진
- [x] db.t4g.micro (프리티어)
- [x] 20GB gp3 스토리지
- [x] Single-AZ (프리티어)
- [x] 자동 백업 7일 보관
- [x] Private subnet 배포
- [x] Deletion protection 활성화
- [x] 초기 DB 생성 (`coffeeshout`)

---

## 6. ElastiCache (Valkey)

### 6.1 ElastiCache 모듈 ✅
- [x] Valkey/Redis/Memcached 지원
- [x] Valkey는 Replication Group 사용
- [x] 프리티어 사양 지원 (cache.t4g.micro)
- [x] Cache Subnet Group 자동 생성
- [x] Parameter Group 설정
- [x] 스냅샷 백업 설정

### 6.2 Production ElastiCache ✅
- [x] Valkey 7.2 엔진
- [x] cache.t4g.micro (프리티어)
- [x] Single node (프리티어)
- [x] 자동 백업 5일 보관
- [x] Private subnet 배포

---

## 7. S3 버킷

### 7.1 S3 버킷 ✅
- [x] S3 버킷 생성 (`coffee-shout`)
- [x] CORS 설정 (QR 코드 접근용)
- [x] Lifecycle 정책 (QR 30일 후 삭제)
- [x] 버저닝 활성화
- [x] AES256 암호화
- [x] Public access 설정 (QR 코드용)

### 7.2 버킷 구조 ✅
- [x] terraform/tfstate/ (Terraform state)
- [x] qr/dev/ (Dev QR 코드)
- [x] qr/prod/ (Prod QR 코드)

---

## 8. IAM 역할

### 8.1 EC2 IAM Role
- [ ] S3 접근 권한 (향후 추가)
- [ ] CloudWatch 권한 (향후 추가)
- [ ] Systems Manager 권한 (향후 추가)

---

## 9. 환경 변수 설정

### 9.1 Production 환경 (terraform.tfvars) ✅
- [x] `key_name` - EC2 Key Pair 이름
- [x] `db_password` - RDS 비밀번호

### 9.2 Development 환경 (terraform.tfvars) ✅
- [x] `key_name` - EC2 Key Pair 이름

### 9.3 애플리케이션 환경 변수 (향후 설정)
**Production**:
- [ ] `MYSQL_URL` - RDS 엔드포인트 (terraform output 참조)
- [ ] `MYSQL_USERNAME=admin`
- [ ] `MYSQL_PASSWORD` - DB 비밀번호
- [ ] `VALKEY_HOST` - Valkey 엔드포인트 (terraform output 참조)
- [ ] `S3_BUCKET_NAME=coffee-shout`

**Development**:
- [ ] `MYSQL_URL=jdbc:mysql://localhost:3306/coffeeshout` (Docker)
- [ ] `REDIS_HOST=localhost` (Docker)
- [ ] `S3_BUCKET_NAME=coffee-shout`

---

## 10. 보안 강화

### 10.1 암호화 ✅
- [x] RDS 저장 데이터 암호화
- [x] EBS 볼륨 암호화
- [x] S3 버킷 암호화

### 10.2 네트워크 보안 ✅
- [x] RDS/Valkey는 Private Subnet 배포
- [x] Security Group으로 접근 제어 (EC2에서만)
- [x] Deletion protection (RDS)

### 10.3 시크릿 관리 (향후)
- [ ] AWS Secrets Manager에 DB 비밀번호 저장
- [ ] EC2 IAM Role에 Secrets Manager 접근 권한 부여

---

## 11. 테스트 및 배포

### 11.1 Backend 설정 ✅
- [x] setup-backend.sh 실행
- [x] S3 버킷 생성 확인
- [x] CORS, Lifecycle 설정 확인

### 11.2 Network 환경 ✅
- [x] Terraform init/plan/apply
- [x] VPC, Subnet 생성 확인
- [x] terraform output 확인

### 11.3 Dev 환경 테스트 ✅
- [x] terraform.tfvars 생성 (key_name)
- [x] Terraform Plan 검토
- [x] Terraform Apply
- [x] EC2 접속 확인 (SSH)
- [x] Elastic IP 확인
- [ ] Docker로 MySQL/Redis 실행
- [ ] 애플리케이션 배포 및 실행
- [ ] Health Check 확인

### 11.4 Production 배포 ✅
- [x] terraform.tfvars 생성 (key_name, db_password)
- [x] Terraform Plan 검토
- [x] Terraform Apply
- [x] EC2, RDS, Valkey 생성 확인
- [x] Elastic IP 확인
- [ ] RDS 연결 테스트
- [ ] Valkey 연결 테스트
- [ ] 애플리케이션 배포 및 실행
- [ ] Health Check 확인

---

## 참고 사항

### 완료된 인프라 구성 ✅
- **Network**: 단일 VPC (Dev + Prod 공용), Public/Private Subnets
- **Dev**: EC2 (t4g.small), Docker, Elastic IP
- **Prod**: EC2 (t4g.small), RDS MySQL (db.t4g.micro), Valkey (cache.t4g.micro), Elastic IP
- **Storage**: S3 (terraform state + QR codes)

### 프리티어 사양
- **EC2**: t4g.small (프리티어 아님, 월 $15 예상)
- **RDS**: db.t4g.micro (프리티어, 750시간/월)
- **Valkey**: cache.t4g.micro (프리티어, 750시간/월)
- **S3**: 5GB 무료

### 중요 포트
- **22**: SSH
- **80**: HTTP
- **443**: HTTPS
- **8080**: 애플리케이션
- **3306**: MySQL (Private)
- **6379**: Valkey (Private)

### 배포 순서 (중요!)
1. **Backend**: setup-backend.sh 실행
2. **Network**: VPC, Subnets 배포
3. **Dev/Prod**: 환경별 리소스 배포 (순서 무관)

### 삭제 순서 (중요!)
1. **Prod/Dev**: 환경별 리소스 삭제 (순서 무관)
2. **Network**: VPC, Subnets 삭제
3. **Backend**: S3 버킷 수동 삭제 (필요시)
