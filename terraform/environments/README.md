# Terraform 환경별 설정 가이드

## 환경 구성

### DEV 환경
- **EC2**: t4g.small
- **MySQL**: Docker 컨테이너 (localhost:3306)
- **Redis**: Docker 컨테이너 (localhost:6379)
- **S3**: 공용 버킷 (dev/ 경로)
- **ALB**: HTTP만
- **비용**: 100% 프리티어

### PROD 환경
- **EC2**: t4g.small + Elastic IP
- **RDS**: MySQL 8.0.43 (db.t3.micro)
- **ElastiCache**: Valkey 8.0 (cache.t3.micro)
- **S3**: 공용 버킷 (prod/ 경로)
- **ALB**: HTTPS (ACM 인증서 필요)
- **비용**: 100% 프리티어

---

## 1. 사전 준비

### 1.1 Terraform 백엔드 초기화

먼저 S3와 DynamoDB를 생성해야 합니다:

```bash
cd terraform/bootstrap
terraform init
terraform apply
```

### 1.2 ACM 인증서 생성 (PROD만)

PROD 환경에서 HTTPS를 사용하려면 ACM 인증서가 필요합니다:

1. AWS Console → Certificate Manager
2. 인증서 요청 → 공개 인증서 요청
3. 도메인 이름 입력
4. DNS 또는 이메일 검증 완료
5. 생성된 ARN을 `terraform.tfvars`에 입력

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

### 2.2 Terraform 실행

```bash
terraform init
terraform plan
terraform apply
```

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

# MySQL/Redis 컨테이너 시작
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
docker compose logs -f redis
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

### 3.2 Terraform 실행

```bash
terraform init
terraform plan
terraform apply
```

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

1. **DEV 환경**: 사용하지 않을 때 EC2 중지
   ```bash
   aws ec2 stop-instances --instance-ids i-xxxxx
   ```

2. **Docker 컨테이너**: 사용하지 않을 때 중지
   ```bash
   docker compose down
   ```

3. **CloudWatch Logs**: 주기적으로 로그 정리
   - DEV: 7일 자동 삭제
   - PROD: 30일 자동 삭제

4. **S3**: 오래된 파일 정리 (Lifecycle 정책 자동 적용)

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
