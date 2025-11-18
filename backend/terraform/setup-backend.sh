#!/bin/bash
set -e

echo "🚀 Terraform Backend 설정 시작..."

BUCKET_NAME="coffee-shout"
REGION="ap-northeast-2"

# S3 버킷 생성 (Object Lock 활성화)
echo "📦 S3 버킷 생성: $BUCKET_NAME (Object Lock 활성화)"
aws s3api create-bucket \
  --bucket $BUCKET_NAME \
  --region $REGION \
  --create-bucket-configuration LocationConstraint=$REGION \
  --object-lock-enabled-for-bucket \
  2>/dev/null || echo "버킷이 이미 존재하거나 생성 실패"

# S3 버킷 버저닝 활성화 (Object Lock은 버저닝 필수)
echo "📦 S3 버킷 버저닝 활성화"
aws s3api put-bucket-versioning \
  --bucket $BUCKET_NAME \
  --versioning-configuration Status=Enabled

# S3 버킷 암호화 활성화
echo "🔒 S3 버킷 암호화 활성화"
aws s3api put-bucket-encryption \
  --bucket $BUCKET_NAME \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# S3 버킷 Public Access 설정 (QR 코드는 public read 필요)
echo "🔒 S3 버킷 Public Access 설정 (QR 코드는 public read 허용)"
aws s3api put-public-access-block \
  --bucket $BUCKET_NAME \
  --public-access-block-configuration \
    BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false

# CORS 설정 (QR 코드 접근용)
echo "🌐 CORS 설정"
aws s3api put-bucket-cors \
  --bucket $BUCKET_NAME \
  --cors-configuration '{
    "CORSRules": [{
      "AllowedHeaders": ["*"],
      "AllowedMethods": ["GET", "PUT", "POST"],
      "AllowedOrigins": ["*"],
      "MaxAgeSeconds": 3000
    }]
  }'

# Lifecycle 설정 (QR 코드 30일 후 삭제)
echo "♻️  Lifecycle 설정 (QR 코드 30일 후 삭제)"
aws s3api put-bucket-lifecycle-configuration \
  --bucket $BUCKET_NAME \
  --lifecycle-configuration '{
    "Rules": [{
      "ID": "delete-old-qr-codes",
      "Status": "Enabled",
      "Filter": {
        "Prefix": "qr/"
      },
      "Expiration": {
        "Days": 30
      }
    }]
  }'

echo ""
echo "✅ Terraform Backend 설정 완료!"
echo ""
echo "📋 생성된 리소스:"
echo "  - S3 버킷: s3://$BUCKET_NAME (Object Lock 활성화)"
echo ""
echo "📁 버킷 구조:"
echo "  - terraform/tfstate/network/ - Network 환경 상태"
echo "  - terraform/tfstate/dev/     - Dev 환경 상태"
echo "  - terraform/tfstate/prod/    - Prod 환경 상태"
echo "  - qr/dev/                    - Dev QR 코드 (30일 후 삭제)"
echo "  - qr/prod/                   - Prod QR 코드 (30일 후 삭제)"
echo ""
echo "🔍 확인 명령어:"
echo "  aws s3 ls s3://$BUCKET_NAME"
echo "  aws s3api get-object-lock-configuration --bucket $BUCKET_NAME"
echo "  aws s3api get-bucket-cors --bucket $BUCKET_NAME"
echo "  aws s3api get-bucket-lifecycle-configuration --bucket $BUCKET_NAME"
echo ""
echo "📝 다음 단계:"
echo "  cd environments/network"
echo "  terraform init"
