# 브랜치 변경사항 요약

**브랜치**: `claude/github-be-setup-011CV5P7bJAk33XEG7gFY1jX`
**베이스 커밋**: `6143697` (docs: README.md)
**작성일**: 2025-11-21

## 📊 변경사항 통계

- **총 커밋 수**: 440개
- **변경된 파일**: 458개
  - 추가된 파일: 437개
  - 수정된 파일: 6개
  - 삭제된 파일: 14개
  - 이동된 파일: 1개
- **코드 변경량**: +24,455줄 / -156줄

## 🎯 개요

이 브랜치는 **Coffee Shout 백엔드의 전체 구조 구현 및 리팩토링**을 포함합니다. 주요 내용은 다음과 같습니다:

1. **CI/CD 파이프라인 구축** - GitHub Actions 및 AWS CodeDeploy 설정
2. **핵심 도메인 기능 구현** - Room, 미니게임(카드/레이싱), 대시보드
3. **아키텍처 리팩토링** - 계층 분리 및 DDD 패턴 적용
4. **인프라 설정** - Redis, WebSocket, 메트릭 수집, 분산 락 등

---

## 📦 주요 변경 카테고리

### 1. CI/CD 및 배포 설정 ✅

#### GitHub Actions 워크플로우
- **`backend-ci.yml`**: 백엔드 CI 파이프라인
  - JDK 21 (Corretto) 사용
  - 컴파일 체크 → 테스트 실행 → 테스트 결과 리포팅
  - `be/dev`, `be/prod` 브랜치 대상

#### AWS CodeDeploy 설정
- **`appspec-dev.yml`** / **`appspec-prod.yml`**
  - EC2 배포 스크립트 정의
  - 배포 생명주기 훅 구성
  - JAR 파일 및 스크립트 배포

#### 배포 스크립트 (`backend/scripts/`)
- `application_start-dev.sh` / `application_start-prod.sh`: 애플리케이션 시작
- `application_stop-dev.sh` / `application_stop-prod.sh`: 애플리케이션 종료
- `before_install.sh`: 배포 전 준비 작업
- `validate_service.sh`: 배포 성공 검증

#### 빌드 설정
- **`buildspec-dev.yml`** / **`buildspec-prod.yml`**: AWS CodeBuild 설정
- **`docker-compose.yml`**: 로컬 개발 환경 (MySQL, Redis 등)

---

### 2. 도메인 기능 구현 🎮

#### 2.1 Room 시스템 (130개 파일)
**패키지**: `coffeeshout.room.*`

##### 핵심 도메인 모델
- **Room**: 방 생성, 플레이어 관리, 게임 상태 관리
- **Player**: 호스트/게스트, 메뉴 선택, 준비 상태
- **Roulette**: 확률 기반 당첨자 선정
- **Menu**: 메뉴 카테고리 및 메뉴 항목 관리
- **JoinCode**: 5자리 초대 코드 생성 (QR 코드 포함)

##### 주요 기능
- 방 생성 및 입장 (초대 코드/QR 코드)
- 플레이어 관리 (참가, 퇴장, 강퇴, 재입장)
- 메뉴 선택 및 변경
- 미니게임 선택 (최대 5개)
- 룰렛 확률 조정 및 당첨자 선정
- 플레이어 색상 관리 (중복 방지)

##### 계층 구조
```
room/
├── domain/           # 도메인 모델 및 비즈니스 로직
│   ├── event/        # 도메인 이벤트
│   ├── menu/         # 메뉴 관련 모델
│   ├── player/       # 플레이어 관련 모델
│   ├── roulette/     # 룰렛 로직
│   ├── repository/   # 레포지토리 인터페이스
│   └── service/      # 도메인 서비스
├── application/      # 애플리케이션 서비스
├── infra/            # 인프라 구현
│   ├── persistence/  # JPA 엔티티 및 레포지토리
│   └── messaging/    # Redis Pub/Sub 핸들러
└── ui/               # 컨트롤러 및 요청/응답 DTO
    ├── request/
    └── response/
```

##### 이벤트 기반 아키텍처
- **도메인 이벤트**: `RoomCreateEvent`, `PlayerKickEvent`, `RouletteSpinEvent` 등
- **브로드캐스트 이벤트**: Redis Pub/Sub을 통한 실시간 상태 동기화
- **WebSocket 메시지**: STOMP를 통한 클라이언트 업데이트

---

#### 2.2 카드 게임 (29개 파일)
**패키지**: `coffeeshout.cardgame.*`

##### 게임 로직
- **CardGame**: 카드 게임 상태 관리
- **Deck**: 덧셈 카드 + 곱셈 카드로 구성된 덱
- **AdditionCard**: 덧셈 카드 (점수 가산)
- **MultiplierCard**: 곱셈 카드 (점수 배수 적용)
- **CardGameRound**: 라운드 관리 (3라운드 진행)

##### 게임 흐름
1. 게임 시작 → 덱 생성 및 카드 분배
2. 각 라운드마다 플레이어가 카드 선택
3. 선택한 카드에 따라 점수 계산 (덧셈 → 곱셈)
4. 3라운드 종료 후 최종 점수로 순위 결정

##### 기술 요소
- **TaskScheduler**: 라운드 타임아웃 관리
- **Redis Stream**: 카드 선택 명령 처리
- **Spring Event**: 게임 상태 변경 이벤트

---

#### 2.3 레이싱 게임 (28개 파일)
**패키지**: `coffeeshout.racinggame.*`

##### 게임 로직
- **RacingGame**: 레이싱 게임 상태 관리
- **Runner**: 각 플레이어의 러너
- **SpeedCalculator**: 탭 속도 기반 이동 거리 계산
- **TapPerSecondSpeedCalculator**: 초당 탭 수를 이동 속도로 변환

##### 게임 흐름
1. 게임 시작 → 러너 생성
2. 플레이어 탭 → 러너 이동
3. 목표 지점 도달 → 순위 결정
4. 최종 순위로 룰렛 확률 조정

##### 기술 요소
- **TaskScheduler**: 게임 타임아웃 관리
- **Redis Pub/Sub**: 탭 명령 이벤트 전파
- **WebSocket**: 실시간 러너 위치 업데이트

---

#### 2.4 미니게임 시스템 (39개 파일)
**패키지**: `coffeeshout.minigame.*`

##### 공통 인터페이스
- **Playable**: 모든 미니게임이 구현해야 하는 인터페이스
- **MiniGameResult**: 게임 결과 (순위 정보)
- **MiniGameScore**: 플레이어별 점수

##### 미니게임 관리
- **MiniGameService**: 미니게임 생성 및 실행 로직
- **MiniGamePersistenceService**: 게임 결과 영속화
- **Command 패턴**: `StartMiniGameCommand`, `SelectCardCommand` 등

##### 이벤트 흐름
```
UI 요청 → Command → CommandHandler → Domain Event → EventHandler → Broadcast
```

---

#### 2.5 대시보드 (8개 파일)
**패키지**: `coffeeshout.dashboard.*`

##### 통계 정보
- **게임 플레이 횟수**: 전체 게임 통계
- **최다 승자**: 가장 많이 당첨된 플레이어
- **최저 확률 승자**: 가장 낮은 확률로 당첨된 플레이어

##### 기술 요소
- **QueryDSL**: 복잡한 통계 쿼리 작성
- **JPA**: 영속성 관리

---

### 3. 전역 인프라 및 설정 (85개 파일)

#### 3.1 WebSocket 설정
**패키지**: `coffeeshout.global.websocket.*`

##### 핵심 컴포넌트
- **WebSocketMessageBrokerConfig**: STOMP over WebSocket 설정
- **StompSessionManager**: 세션 관리 (연결, 구독, 연결 해제)
- **PlayerDisconnectionService**: 플레이어 연결 해제 처리
- **DelayedPlayerRemovalService**: 지연된 플레이어 제거 (재연결 대응)

##### 이벤트 리스너
- `SessionConnectEventListener`: 연결 이벤트 처리
- `SessionDisconnectEventListener`: 연결 해제 이벤트 처리
- `SessionSubscribeEventListener`: 구독 이벤트 처리

##### Graceful Shutdown
- `WebSocketGracefulShutdownHandler`: 서버 종료 시 WebSocket 연결 정상 종료

---

#### 3.2 Redis 설정
**패키지**: `coffeeshout.global.config.redis.*`

##### Redis 기능
- **Pub/Sub**: 이벤트 브로드캐스트
- **Streams**: 명령 처리 (카드 선택, 탭 등)
- **Distributed Lock**: 동시성 제어 (Redisson)
- **Session Storage**: JoinCode 저장

##### 컴포넌트
- **RedisConfig**: Redis 연결 설정
- **RedissonConfig**: Redisson 클라이언트 설정
- **StreamConsumerConfig**: Redis Stream 컨슈머 설정
- **EventTopicRegistry**: 이벤트 토픽 관리
- **TopicManager**: 토픽 이름 생성 및 관리

---

#### 3.3 메트릭 및 관찰성
**패키지**: `coffeeshout.global.metric.*`, `coffeeshout.global.trace.*`

##### 메트릭 수집
- **HttpMetricService**: HTTP 요청 메트릭
- **WebSocketMetricService**: WebSocket 메시지 메트릭
- **GameDurationMetricService**: 게임 진행 시간 메트릭

##### 분산 추적
- **Micrometer + Observation API**: 관찰성 향상
- **TraceInfo**: 추적 정보 추출
- **MessageMappingTracingAspect**: WebSocket 메시지 매핑 추적

---

#### 3.4 예외 처리
**패키지**: `coffeeshout.global.exception.*`

##### 전역 예외 핸들러
- **RestExceptionHandler**: REST API 예외 처리
- **WebSocketExceptionHandler**: WebSocket 예외 처리

##### 커스텀 예외
- `InvalidArgumentException`: 잘못된 인자
- `InvalidStateException`: 잘못된 상태
- `NotExistElementException`: 존재하지 않는 요소
- `QRCodeGenerationException`: QR 코드 생성 실패
- `StorageServiceException`: 저장소 서비스 오류

---

#### 3.5 기타 설정
**패키지**: `coffeeshout.global.config.*`

- **AsyncConfig**: 비동기 작업 설정
- **SwaggerConfig**: API 문서 자동 생성
- **QueryDslConfig**: QueryDSL 설정
- **WebMvcConfig**: CORS 설정
- **ObservationConfig**: 관찰성 설정
- **InitConfig**: 초기 데이터 로드 (메뉴 카테고리 및 메뉴)

---

### 4. 아키텍처 리팩토링 🏗️

#### 4.1 계층 분리
**리팩토링 커밋**:
- `0925325`: Domain Service 네이밍 개선 및 Application Layer 도입
- `7ab2f44`: ApplicationService 네이밍을 Service로 통일
- `d1d1bf0`: application 패키지 정리

#### 계층 구조

```
┌─────────────────────────────────────┐
│      UI Layer (Presentation)        │
│  - Controllers                      │
│  - Request/Response DTOs            │
│  - MessagePublisher (WebSocket)     │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│      Application Layer               │
│  - Application Services              │
│  - Use Case 조율                     │
│  - 트랜잭션 경계                      │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│       Domain Layer                   │
│  - Domain Models                     │
│  - Domain Services                   │
│  - Domain Events                     │
│  - Business Logic                    │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│      Infrastructure Layer            │
│  - JPA Repositories                  │
│  - Redis Publishers/Subscribers      │
│  - External Services (S3, QR 생성)   │
└─────────────────────────────────────┘
```

#### 4.2 서비스 네이밍 규칙
- **Application Service**: `XxxService` (예: `RoomService`, `CardGameService`)
- **Domain Service**: `XxxCommandService`, `XxxQueryService` (예: `RoomCommandService`, `RoomQueryService`)
- **Persistence Service**: `XxxPersistenceService` (예: `RoomPersistenceService`)

#### 4.3 이벤트 기반 아키텍처
**리팩토링 커밋**:
- `acd0d40`: STOMP 브로드캐스트를 Spring Event 패턴으로 중앙화
- `f54e59f`: MessagePublisher를 UI Layer로 이동 및 API 문서 중앙화

##### 변경 전
```java
// Service에서 직접 WebSocket 메시지 전송
simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, message);
```

##### 변경 후
```java
// 1. Domain에서 이벤트 발행
applicationEventPublisher.publishEvent(new PlayerListChangedEvent(...));

// 2. EventHandler에서 이벤트 처리
@EventListener
public void handle(PlayerListChangedEvent event) {
    // 비즈니스 로직 처리
}

// 3. UI Layer에서 WebSocket 메시지 전송
messagePublisher.publishPlayerListChanged(roomId, players);
```

#### 장점
- 관심사 분리: 도메인 로직과 전송 로직 분리
- 테스트 용이성: 이벤트만 검증하면 됨
- 확장성: 새로운 이벤트 핸들러 추가 용이

---

### 5. 테스트 구조 개선 🧪

**리팩토링 커밋**: `8261197` - 테스트 패키지 구조 정리 및 LogAspect 수정

#### 테스트 구조
```
test/
├── fixture/                    # 테스트 픽스처 및 헬퍼
│   ├── TestDataHelper.java
│   ├── PlayerFixture.java
│   ├── RoomFixture.java
│   └── WebSocketIntegrationTestSupport.java
├── global/
│   ├── ServiceTest.java                # 서비스 테스트 베이스 클래스
│   ├── WebMvcIntegrationTest.java      # REST API 테스트 베이스
│   └── config/
│       ├── IntegrationTestConfig.java
│       ├── ServiceTestConfig.java
│       └── TestContainerConfig.java    # Testcontainers 설정
└── [domain]/                   # 도메인별 테스트
```

#### 테스트 유형
1. **단위 테스트**: 도메인 로직 검증
2. **서비스 테스트**: 서비스 계층 검증 (Mockito 사용)
3. **통합 테스트**: 실제 DB, Redis 사용 (Testcontainers)
4. **WebSocket 통합 테스트**: STOMP 클라이언트로 실제 메시지 송수신 테스트

---

## 🗂️ 파일 변경 세부사항

### 루트 레벨 변경
- `.coderabbit.yaml`: CodeRabbit AI 리뷰 설정 수정
- `.gitignore`: 빌드 산출물, IDE 설정 추가
- `README.md`: 프로젝트 소개 업데이트

### 백엔드 설정 파일
- `backend/build.gradle.kts`: 의존성 추가 (QueryDSL, Redisson, Micrometer 등)
- `backend/gradle.properties`: Gradle 속성 설정
- `backend/settings.gradle.kts`: 프로젝트 이름 설정

### 환경별 설정
- `application-local.yml`: 로컬 개발 환경
- `application-dev.yml`: 개발 서버 환경
- `application-prod.yml`: 운영 서버 환경

### 데이터베이스 마이그레이션
- `V1__init_schema.sql`: 초기 스키마 생성
- `V2__mini_game_score_type_change.sql`: 미니게임 점수 타입 변경

### 초기 데이터
- `menu-category-data.yml`: 메뉴 카테고리 초기 데이터
- `menu-data.yml`: 메뉴 초기 데이터

---

## 🔑 핵심 기술 스택

### 백엔드
- **Java 21** (Amazon Corretto)
- **Spring Boot 3.x**
- **Spring Data JPA** + **QueryDSL**
- **Spring WebSocket** (STOMP)
- **Redis** (Pub/Sub + Streams + Distributed Lock)
- **Flyway** (DB 마이그레이션)
- **Micrometer** (메트릭 수집)

### 인프라
- **MySQL**: 주요 데이터 저장소
- **Redis**: 세션, 이벤트, 명령 처리
- **AWS S3**: QR 코드 이미지 저장
- **AWS CodeDeploy**: 자동 배포
- **GitHub Actions**: CI 파이프라인

### 테스트
- **JUnit 5**
- **Mockito**
- **Testcontainers** (MySQL, Redis)
- **AssertJ**

---

## 🚀 다음 단계 제안

### 1. 성능 최적화
- [ ] Redis Pub/Sub 메시지 크기 최적화
- [ ] JPA N+1 쿼리 문제 점검
- [ ] WebSocket 연결 수 모니터링

### 2. 안정성 개선
- [ ] 재시도 로직 추가 (Redis 연결 실패 시)
- [ ] Circuit Breaker 패턴 적용 (외부 서비스 호출)
- [ ] 에러 로깅 및 알림 설정

### 3. 문서화
- [ ] API 문서 작성 (Swagger 활용)
- [ ] 아키텍처 다이어그램 작성
- [ ] 배포 가이드 작성

### 4. 모니터링
- [ ] Prometheus + Grafana 대시보드 구성
- [ ] 알람 규칙 설정 (높은 에러율, 느린 응답 등)
- [ ] 분산 추적 시각화 (Zipkin/Jaeger)

### 5. 테스트 커버리지
- [ ] 단위 테스트 커버리지 80% 이상 달성
- [ ] 통합 테스트 시나리오 추가
- [ ] E2E 테스트 자동화

---

## 📝 주의사항

### 배포 시
1. **환경별 설정 확인**: `application-{profile}.yml` 파일의 민감한 정보가 노출되지 않도록 주의
2. **Redis 연결 정보**: 환경 변수로 관리 필요
3. **AWS 자격 증명**: CodeDeploy 및 S3 접근을 위한 IAM 역할 설정
4. **데이터베이스 마이그레이션**: Flyway 스크립트 순서 및 내용 검증

### 개발 시
1. **계층 준수**: UI → Application → Domain → Infra 순서 준수
2. **이벤트 사용**: 모듈 간 통신은 이벤트 기반으로
3. **도메인 로직 집중**: 비즈니스 로직은 도메인 계층에
4. **테스트 작성**: 새로운 기능 추가 시 테스트 필수

---

## 🙋 질문 및 피드백

이 브랜치에 대한 질문이나 피드백이 있다면 팀 채널에 남겨주세요!

- 아키텍처 변경에 대한 의견
- 리팩토링 방향에 대한 제안
- 추가로 필요한 문서나 설명

---

**작성자**: Claude AI
**마지막 업데이트**: 2025-11-21
