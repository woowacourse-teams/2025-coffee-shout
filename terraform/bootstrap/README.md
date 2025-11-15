# Terraform 백엔드 부트스트랩

## 개요

Terraform State를 S3에 저장하고 DynamoDB로 동시 수정을 방지하기 위한 백엔드 인프라를 생성합니다.

## 사전 요구사항

- AWS CLI 설정 완료
- 적절한 IAM 권한 (S3, DynamoDB 생성 권한)

## 실행 방법

### 1. 백엔드 인프라 생성 (최초 1회만 실행)

```bash
cd terraform/bootstrap

# Terraform 초기화
terraform init

# 실행 계획 확인
terraform plan

# 백엔드 인프라 생성
terraform apply
```

### 2. 출력된 백엔드 설정 정보 확인

`terraform apply` 완료 후 출력되는 백엔드 설정 정보를 복사합니다.

```
Outputs:

backend_config = <<EOT

terraform {
  backend "s3" {
    bucket         = "coffeeshout-terraform-state"
    key            = "ENV_NAME/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "coffeeshout-terraform-locks"
    encrypt        = true
  }
}
EOT
```

### 3. 환경별 backend.tf 파일 생성

이 설정을 각 환경(`dev`, `prod`)의 `backend.tf` 파일에 추가합니다.

**개발 환경 (`environments/dev/backend.tf`)**:
```hcl
terraform {
  backend "s3" {
    bucket         = "coffeeshout-terraform-state"
    key            = "dev/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "coffeeshout-terraform-locks"
    encrypt        = true
  }
}
```

**운영 환경 (`environments/prod/backend.tf`)**:
```hcl
terraform {
  backend "s3" {
    bucket         = "coffeeshout-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "coffeeshout-terraform-locks"
    encrypt        = true
  }
}
```

## 생성되는 리소스

### S3 버킷
- **이름**: `coffeeshout-terraform-state`
- **용도**: Terraform State 파일 저장
- **특징**:
  - 버저닝 활성화 (State 파일 이력 관리)
  - 서버 측 암호화 (AES256)
  - 퍼블릭 액세스 완전 차단
- **비용**: 프리티어 5GB 무료 (State 파일은 몇 KB 수준)

### DynamoDB 테이블
- **이름**: `coffeeshout-terraform-locks`
- **용도**: Terraform State 동시 수정 방지 (Locking)
- **특징**:
  - PAY_PER_REQUEST 모드 (사용한 만큼만 과금)
  - 단일 속성: LockID (String)
- **비용**: 프리티어 25 읽기/쓰기 유닛 무료

## 백엔드 인프라 삭제

⚠️ **주의**: 백엔드 인프라를 삭제하면 모든 환경의 State 파일이 손실됩니다!

```bash
cd terraform/bootstrap

# S3 버킷 버저닝 비활성화
aws s3api put-bucket-versioning \
  --bucket coffeeshout-terraform-state \
  --versioning-configuration Status=Suspended

# S3 버킷 비우기
aws s3 rm s3://coffeeshout-terraform-state --recursive

# Terraform으로 리소스 삭제
terraform destroy
```

## 트러블슈팅

### S3 버킷 이름 중복 에러

S3 버킷 이름은 전 세계적으로 고유해야 합니다. 이미 사용 중인 이름이라면 `backend.tf`에서 버킷 이름을 변경하세요.

```hcl
resource "aws_s3_bucket" "terraform_state" {
  bucket = "coffeeshout-terraform-state-YOUR_UNIQUE_ID"
  # ...
}
```

### DynamoDB 테이블 Lock 에러

여러 명이 동시에 `terraform apply`를 실행하면 Lock 에러가 발생합니다. 다른 사람의 작업이 끝날 때까지 기다리거나, AWS 콘솔에서 Lock을 수동으로 해제할 수 있습니다.

## 참고 자료

- [Terraform S3 Backend 문서](https://www.terraform.io/docs/language/settings/backends/s3.html)
- [AWS S3 프리티어](https://aws.amazon.com/s3/pricing/)
- [AWS DynamoDB 프리티어](https://aws.amazon.com/dynamodb/pricing/)
