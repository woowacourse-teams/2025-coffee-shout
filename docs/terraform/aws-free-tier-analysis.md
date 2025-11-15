# AWS 프리티어 분석 및 커피빵 프로젝트 적용

## 📋 AWS 프리티어 개요

AWS 프리티어는 신규 AWS 사용자가 클라우드 서비스를 체험하고 학습할 수 있도록 제공하는 무료 프로그램입니다.

**프리티어 유형**:
1. **12개월 무료**: 가입 후 12개월간 제한된 용량 내 무료
2. **상시 무료**: 계정 나이와 관계없이 항상 무료
3. **체험판**: 특정 기간 동안만 무료

---

## 🎯 커피빵 프로젝트 관련 프리티어 서비스

### 1. 컴퓨팅 (Compute)

#### Amazon EC2
- **프리티어**: t2.micro 또는 t3.micro 인스턴스 **월 750시간** (12개월)
- **스펙**: 1 vCPU, 1GB RAM
- **적용**:
  - ✅ 750시간 = 한 달 내내 1대 가동 가능
  - ✅ 또는 2대를 절반씩 (각 375시간)
  - ⚠️ **요구사항**: t4g.small (2 vCPU, 2GB RAM) → **프리티어 범위 초과**

**💡 권장사항**:
- DEV 환경: t3.micro (프리티어)
- PROD 환경: 초기에는 t3.micro로 시작, 트래픽 증가 시 t4g.small로 업그레이드

---

### 2. 데이터베이스 (Database)

#### Amazon RDS
- **프리티어**: db.t2.micro 또는 db.t3.micro 인스턴스 **월 750시간** (12개월)
- **스토리지**: 20GB 범용 SSD (gp2)
- **백업**: 20GB 백업 스토리지
- **지원 엔진**: MySQL, MariaDB, PostgreSQL, Oracle BYOL, SQL Server Express
- **적용**:
  - ✅ MySQL 8.0 사용 가능
  - ✅ db.t3.micro (1 vCPU, 1GB RAM)
  - ✅ 요구사항과 일치!

**💡 권장사항**:
- db.t3.micro, MySQL 8.0.43 사용
- Single-AZ 배치 (Multi-AZ는 프리티어 초과)
- 스토리지 20GB 이내로 관리

---

### 3. 캐싱 (Caching)

#### Amazon ElastiCache
- **프리티어**: cache.t2.micro 또는 cache.t3.micro 노드 **월 750시간** (12개월)
- **메모리**: 약 500MB~600MB
- **엔진**: Redis, Valkey (Redis 호환)
- **적용**:
  - ✅ **Valkey 사용 가능!** (요구사항과 일치)
  - ✅ cache.t3.micro 노드

**💡 권장사항**:
- cache.t3.micro, Valkey 엔진 사용
- Single-AZ 배치
- 백업 비활성화 (프리티어 범위 내 유지)

---

### 4. 로드 밸런싱 (Load Balancing)

#### Elastic Load Balancer (ELB)
- **프리티어**: **월 750시간** (12개월)
- **데이터 처리**: 15GB
- **타입**: Application Load Balancer (ALB), Network Load Balancer (NLB)
- **적용**:
  - ✅ ALB 사용 가능 (요구사항과 일치)
  - ✅ 750시간 = 한 달 내내 1대 가동 가능

**💡 권장사항**:
- Application Load Balancer 1대
- 데이터 전송량 15GB 이내로 관리

---

### 5. 스토리지 (Storage)

#### Amazon S3
- **프리티어**: 5GB 표준 스토리지 (상시 무료)
- **요청**:
  - GET 요청 20,000건/월
  - PUT 요청 2,000건/월
- **적용**:
  - Terraform State 파일 저장 (몇 KB 수준)
  - 애플리케이션 파일 저장 (이미지, 로그 등)

#### Amazon EBS
- **프리티어**: 30GB 범용 SSD 또는 마그네틱 스토리지 (12개월)
- **스냅샷**: 1GB
- **I/O**: 200만 I/O
- **적용**:
  - ✅ EC2 인스턴스당 15GB 요구사항 → 2대면 30GB (프리티어 범위 내!)

**💡 권장사항**:
- DEV EC2: 15GB EBS
- PROD EC2: 15GB EBS
- 총 30GB (프리티어 한도 정확히 사용)

---

### 6. CI/CD

#### AWS CodePipeline
- **프리티어**: **1개 활성 파이프라인 무료** (상시 무료)
- **적용**:
  - ⚠️ 요구사항: 2개 파이프라인 (DEV용, PROD용)
  - ❌ 2개째부터는 월 $1/파이프라인

#### AWS CodeBuild
- **프리티어**: 월 100분 빌드 시간 (상시 무료)
- **적용**:
  - ⚠️ 100분은 매우 적음 (하루 3~4번 빌드 시 초과 가능)

**💡 권장사항**:
- **Option A**: GitHub Actions 사용 (무료, 월 2,000분)
- **Option B**: CodePipeline 1개만 사용 (PROD용), DEV는 GitHub Actions

---

### 7. 기타 서비스

#### Amazon DynamoDB
- **프리티어**: 25GB 스토리지, 25 읽기/쓰기 유닛 (상시 무료)
- **적용**: Terraform State Lock용으로 사용 (거의 무료)

#### Amazon CloudWatch
- **프리티어**:
  - 10개 지표
  - 10개 알람
  - 100만 API 요청
- **적용**: 기본 모니터링용으로 충분

---

## ⚠️ 프리티어에 포함되지 않는 서비스

### NAT Gateway
- **비용**: 시간당 $0.045 + 데이터 처리 비용
- **월 예상**: 약 $35~$40
- **해결 방안**:
  - Option A: NAT Instance 사용 (t4g.nano, 월 $3~$5)
  - Option B: Private 서브넷 제거, Security Group으로 보안 강화

### CloudFront
- **프리티어**: 월 50GB 데이터 전송, 200만 요청 (12개월)
- **적용**: 프론트엔드에서 사용 가능하지만, 요구사항에서는 외부 CDN 사용

---

## 💰 커피빵 프로젝트 비용 예측

### 🎉 프리티어 최대 활용 구성

| 리소스 | 프리티어 한도 | 사용 계획 | 비용 |
|--------|-------------|---------|------|
| **EC2 (DEV)** | t3.micro 750h | t3.micro 24/7 | $0 |
| **EC2 (PROD)** | 포함됨 | t3.micro 24/7 | $0 |
| **RDS** | db.t3.micro 750h | db.t3.micro, 20GB | $0 |
| **ElastiCache** | cache.t3.micro 750h | cache.t3.micro | $0 |
| **ALB** | 750h, 15GB | ALB 1대 | $0 |
| **EBS** | 30GB | 15GB × 2 = 30GB | $0 |
| **S3** | 5GB | State 파일 저장 | $0 |
| **DynamoDB** | 25GB | State Lock | $0 |
| **NAT Gateway** | ❌ 없음 | 제거 (Public 배치) | $0 |
| **CodePipeline** | 1개 무료 | GitHub Actions 사용 | $0 |
| | | **총합** | **$0/월** |

### ⚠️ 제한사항 및 권장사항

1. **EC2 인스턴스 타입**
   - 프리티어: t3.micro (1 vCPU, 1GB RAM)
   - 요구사항: t4g.small (2 vCPU, 2GB RAM)
   - **권장**: 초기에는 t3.micro로 시작, 필요시 업그레이드

2. **NAT Gateway 대안**
   - Private 서브넷 사용 시 NAT Gateway 필요 → 고비용
   - **권장**: PROD도 Public Subnet 배치, Security Group으로 보안 강화
   - 대안: NAT Instance (t4g.nano, 월 $3~$5)

3. **CodePipeline**
   - 프리티어: 1개만 무료
   - 요구사항: 2개 (DEV, PROD)
   - **권장**: GitHub Actions 사용 (무료, 월 2,000분)

4. **프리티어 기간**
   - 대부분 서비스: 12개월
   - 이후 정상 요금 청구됨
   - **권장**: 알림 설정으로 사용량 모니터링

---

## 📊 최종 권장 아키텍처 (프리티어 최대 활용)

```
┌─────────────────────────────────────────────────────────────┐
│                    VPC (10.0.0.0/16)                         │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Public Subnet (10.0.1.0/24)             │   │
│  │                                                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │   │
│  │  │  DEV EC2     │  │  PROD EC2    │  │    ALB    │  │   │
│  │  │  t3.micro    │  │  t3.micro    │  │           │  │   │
│  │  └──────────────┘  └──────────────┘  └───────────┘  │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │             Private Subnet (10.0.2.0/24)             │   │
│  │                                                      │   │
│  │  ┌──────────────┐  ┌──────────────────────────┐     │   │
│  │  │     RDS      │  │     ElastiCache         │     │   │
│  │  │db.t3.micro   │  │   cache.t3.micro        │     │   │
│  │  │  MySQL 8.0   │  │      (Valkey)           │     │   │
│  │  └──────────────┘  └──────────────────────────┘     │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘

CI/CD: GitHub Actions (무료)
State: S3 + DynamoDB (프리티어)
모니터링: CloudWatch (프리티어)
```

### 특징
- ✅ **100% 프리티어** (12개월간 무료)
- ✅ EC2 2대 (DEV + PROD)
- ✅ RDS, ElastiCache 포함
- ✅ ALB로 로드 밸런싱
- ✅ Private Subnet으로 DB 보안
- ✅ NAT Gateway 없음 (비용 절감)

### 보안 고려사항
- PROD EC2가 Public Subnet에 있지만, Security Group으로 ALB에서만 접근 허용
- SSH는 특정 IP에서만 허용하도록 설정
- RDS, ElastiCache는 Private Subnet에 배치

---

## 🔔 프리티어 사용량 모니터링

### 알림 설정 권장
1. AWS Billing 대시보드에서 프리티어 사용량 확인
2. 85% 도달 시 알림 설정
3. CloudWatch 알람으로 리소스 사용량 모니터링

### 모니터링 대상
- EC2 사용 시간 (월 750시간 한도)
- RDS 사용 시간 (월 750시간 한도)
- ElastiCache 사용 시간 (월 750시간 한도)
- EBS 스토리지 (30GB 한도)
- 데이터 전송량

---

## 📌 중요 사항

⚠️ **프리티어 한도는 계정별 합산**
- 환경별(DEV/PROD)이 아닌 **계정 전체**에 적용
- EC2 750시간 = DEV + PROD 합쳐서 750시간

⚠️ **12개월 이후 정상 요금 청구**
- 프리티어 종료 전 비용 최적화 계획 필요
- 사용하지 않는 리소스 정리

⚠️ **프리티어 초과 시 즉시 과금**
- 실수로 t3.small 사용 시 바로 과금
- 항상 인스턴스 타입 확인

---

## 참고 자료

- [AWS Free Tier 공식 페이지](https://aws.amazon.com/free/)
- [AWS Free Tier 사용량 모니터링](https://console.aws.amazon.com/billing/home#/freetier)
- [AWS Pricing Calculator](https://calculator.aws/)
