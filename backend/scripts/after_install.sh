#!/bin/bash
set -e
export PATH="/usr/bin:/bin:$PATH"

echo "=== [AFTER_INSTALL] 애플리케이션 설정 ==="

cd /opt/coffee-shout

# TODO 환경 변수에 따른 설정 파일 선택 설정 필요
#ENVIRONMENT=${DEPLOYMENT_GROUP_NAME:-dev}
#echo "배포 환경: $ENVIRONMENT"
#
## 환경별 설정 파일 복사
#if [ -f "config/application-$ENVIRONMENT.yml" ]; then
#    cp config/application-$ENVIRONMENT.yml app/application.yml
#    echo "환경별 설정 파일이 적용되었습니다: $ENVIRONMENT"
#else
#    echo "⚠️ 환경별 설정 파일이 없습니다. 기본 설정을 사용합니다."
#fi

# JAR 파일 확인
if [ -f "app/coffee-shout-backend.jar" ]; then
    ln -sf coffee-shout-backend.jar app/coffee-shout.jar
    echo "JAR 파일 심볼릭 링크 생성: coffee-shout-backend.jar"
else
    echo "❌ JAR 파일을 찾을 수 없습니다!"
    exit 1
fi

echo "=== [AFTER_INSTALL] 완료 ==="
