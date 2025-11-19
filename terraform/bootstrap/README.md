# Terraform Bootstrap

이 디렉토리는 Terraform state를 저장할 S3 버킷을 생성합니다.

## 필요성

Terraform은 인프라 상태를 state 파일에 저장합니다. 팀 협업을 위해:
- **S3 버킷**: state 파일을 원격 저장
- **S3 네이티브 locking**: Terraform 1.10+부터 DynamoDB 없이 S3만으로 state locking 지원

## 생성되는 리소스

### Dev 환경
- S3 버킷: `coffeeshout-terraform-state-dev`

### Prod 환경
- S3 버킷: `coffeeshout-terraform-state-prod`

### 주요 특징
- **비용 절감**: DynamoDB 비용 없음
- **간소화**: 추가 리소스 관리 불필요
- **S3 conditional writes**: `.tflock` 파일로 locking 처리

## 사용법

### 1. Bootstrap 리소스 생성

```bash
cd terraform/bootstrap
terraform init
terraform plan
terraform apply
```

### 2. 환경별 Terraform 초기화

Bootstrap이 완료되면 각 환경에서 backend를 초기화합니다:

```bash
# Dev 환경
cd ../environments/dev
terraform init

# Prod 환경
cd ../environments/prod
terraform init
```

## 주의사항

1. **Terraform 버전**: 1.10 이상 필요 (S3 네이티브 locking 지원)
2. **최초 1회만 실행**: 이 bootstrap은 프로젝트당 한 번만 실행하면 됩니다.
3. **State 파일 관리**: bootstrap 디렉토리의 `terraform.tfstate` 파일은 로컬에 저장됩니다. 이 파일을 안전하게 보관하거나 팀원과 공유하세요.
4. **삭제 주의**: 이 리소스를 삭제하면 모든 환경의 Terraform state를 잃게 됩니다.

## 리소스 삭제 (주의!)

Bootstrap 리소스를 삭제하려면:

```bash
# 먼저 각 환경의 모든 리소스를 삭제해야 합니다
cd ../environments/dev
terraform destroy

cd ../environments/prod
terraform destroy

# 그 다음 bootstrap 리소스 삭제
cd ../../bootstrap
terraform destroy
```

⚠️ **경고**: S3 버킷에 state 파일이 있는 경우 삭제가 실패할 수 있습니다. 버킷을 비운 후 다시 시도하세요.
