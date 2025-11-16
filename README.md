# ☕ 커피빵 (CoffeeShout)

<div align="center">
  <img src="https://github.com/user-attachments/assets/ab912632-5b02-4743-a6b0-dab08d6b15d3" alt="커피빵 로고" style="width: 100%; max-width: 100%;" />
</div>

## 똥손도 즐기는 커피빵 전쟁!

점심시간마다 반복되는 '오늘 커피는 누가 살까?' 고민, 지루하지 않으신가요?

커피빵은 **커피 내기를 간편하고 유쾌하게** 즐길 수 있도록 만든 플랫폼입니다.

단순한 뽑기, 미니 게임 그리고 룰렛 시스템으로 더욱 재밌는 경험을 제공합니다.

👉🏻[게임하러가기](https://coffee-shout.com)

## 🎯 서비스 흐름

<img width="7680" height="12960" alt="커피빵 리드미" src="https://github.com/user-attachments/assets/d7355bce-a09a-4f53-8d63-b5b152e24230" />

## 🛠 기술 스택

### 🌐 FrontEnd
<img width="4604" height="2544" alt="image" src="https://github.com/user-attachments/assets/6c91653d-dfa0-4473-a1d9-b2ea100cae87" />

### 🍃 BackEnd
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/bb20bf91-5ddb-408d-a4e3-a0367c76132c" />

### ⚙️ Infra
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/48910643-33ba-409d-b98b-5d274e93e40d" />

## 📌 Infra Design 

### CI / CD
<img width="1000" height="1530" alt="image" src="https://github.com/user-attachments/assets/7c52feab-d94e-432a-bc3f-453d6f902e14" />

### Application
<img width="1000" height="1692" alt="image" src="https://github.com/user-attachments/assets/52f80fd5-c77a-43b8-a988-159e0c8866c6" />

## 🏗️ Infrastructure as Code (Terraform)

AWS 인프라를 Terraform으로 관리합니다. 자세한 내용은 [Terraform 가이드](./terraform/environments/README.md)를 참고하세요.

### 주요 구성 (15개 모듈)
- **컴퓨팅**: EC2, ALB
- **데이터베이스**: RDS MySQL 8.0, ElastiCache Valkey 8.0
- **CI/CD**: CodePipeline, CodeBuild, CodeDeploy
- **모니터링**: CloudWatch Alarms, Lambda Slack 알림
- **보안**: SSM Parameter Store, Secrets Manager, Security Groups
- **스토리지**: S3 버킷

### 빠른 시작
```bash
# 1. GitHub CodeStar Connection 생성 (AWS Console)
# 2. SSM Parameter Store에 환경변수 등록
# 3. Terraform 배포
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
vim terraform.tfvars  # 실제 값으로 수정
terraform init && terraform apply
```

자세한 배포 가이드는 [terraform/environments/README.md](./terraform/environments/README.md)를 참고하세요.

## 👥 멤버

### 프론트엔드


| <img src="https://github.com/user-attachments/assets/c0694fc2-3078-4417-ba7b-2f7a66af1cc8" width="130" height="130"> | <img src ="https://github.com/user-attachments/assets/f95731c4-2cd3-41f4-9d9b-b695bc48b372" width="130" height="130"> | <img src ="https://github.com/user-attachments/assets/b2325a15-4771-48c2-b1a8-52217f4ee92b" width="130" height="130"> |
| :---------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------: | :-----------------------------------------------------------------------------------------: |
|                         [니야](https://github.com/sooyeoniya)                         |                          [메리](https://github.com/rosielsh)                          |                             [다이앤](https://github.com/Daeun-100)                             |

### 백엔드

| <img src="https://github.com/user-attachments/assets/431c8211-6ca8-4599-a5d0-46d292c1abe4" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/1336fce2-2faf-4eee-ba7c-d2a4a99e06e0" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/7819232f-1029-40b4-bca8-19a895df4123" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/ec37aec0-c270-47af-817d-18f30edb504a" width="130" height="130"> |
| :---------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------: |
|                             [한스](https://github.com/20HyeonsuLee)                              |                            [엠제이](https://github.com/theminjunchoi)                             |                            [꾹이](https://github.com/kiwoook)                             |                             [루키](https://github.com/junhaa)                             |


