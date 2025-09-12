# Redis 설정 가이드

## 개요
Room 도메인 객체들을 Redis에 저장해서 스케일아웃 시 서버 간 데이터 공유가 가능하도록 설정.

## 저장 대상
### Redis에 저장되는 것들 (Room 도메인)
- `Room` - 메인 도메인 객체
- `JoinCode` - 방 참여 코드  
- `Player`, `Players`, `PlayerName`, `Winner` - 플레이어 관련
- `SelectedMenu` - 플레이어가 선택한 메뉴 (Player 안에 포함)
- `MiniGameResult`, `MiniGameScore` - 미니게임 결과
- `CardGame`, `CardGameRound` 등 - 카드게임 관련

### 메모리에 유지되는 것들 (마스터 데이터)
- `Menu`, `MenuCategory`, `ProvidedMenu`, `CustomMenu` - 메뉴 마스터 데이터
- `StompSessionManager` - WebSocket 세션 관리
- 스케줄링 관련 객체들

## 사용법

### 1. 메모리 모드 (기본)
```bash
# application.yml의 기본 설정 사용
java -jar app.jar
# 또는 명시적으로
java -jar app.jar --spring.profiles.active=local
```

### 2. Redis 모드
```bash
# Redis 프로파일 활성화
java -jar app.jar --spring.profiles.active=redis

# 환경변수와 함께
REDIS_HOST=redis.example.com java -jar app.jar --spring.profiles.active=redis
```

### 3. 개발 환경에서 테스트
```bash
# 로컬 Redis 사용 (SSL 비활성화)
java -jar app.jar --spring.profiles.active=local,redis
```

## 환경변수 설정

### Redis 연결 정보
- `REDIS_HOST`: Redis 서버 주소 (기본값: localhost)
- Redis 포트는 6379 고정

### AWS S3 관련 
- `S3_BUCKET_NAME`: S3 버킷명
- `S3_QR_KEY_PREFIX`: QR 코드 S3 키 프리픽스

## Redis 데이터 구조

### Key 패턴
```
room:{joinCode}        - Room 객체 저장
joincode:{joinCode}    - JoinCode 중복 체크용
```

### TTL 설정
- 모든 Room 관련 데이터: 24시간 자동 삭제
- 게임 중일 때는 TTL 연장 가능

## 주의사항

### 1. 직렬화 이슈
- Jackson2JsonRedisSerializer 사용
- 객체 구조 변경 시 기존 Redis 데이터와 충돌 가능
- 버전 업그레이드 시 Redis flush 권장

### 2. 성능 고려사항  
- Room 저장 시 모든 Player 데이터가 함께 저장됨
- 대용량 데이터일 경우 네트워크 지연 발생 가능
- 빈번한 조회보다는 상태 저장 목적으로 사용

### 3. 데이터 정합성
- Room 단위로만 저장하므로 부분 업데이트 불가
- Room 전체를 다시 저장해야 함

## 모니터링

### 로그 확인
```yaml
logging:
  level:
    coffeeshout.global.config.redis: debug  # Redis 디버그 로그
    org.springframework.data.redis: info    # Spring Redis 로그
```

### Redis 상태 확인
```bash
# Redis CLI로 확인
redis-cli

# 저장된 Room 개수
KEYS room:*

# 특정 Room 조회  
GET room:ABC12

# TTL 확인
TTL room:ABC12
```

## 마이그레이션 가이드

### 메모리 → Redis 전환
1. Redis 서버 준비
2. 프로파일 변경: `local` → `redis`
3. 기존 진행 중인 게임들은 초기화됨 (Redis에 데이터 없음)
4. 새로 생성되는 Room부터 Redis 저장

### Redis → 메모리 롤백
1. 프로파일 변경: `redis` → `local`  
2. Redis 데이터는 TTL로 자동 삭제됨
3. 기존 진행 중인 게임들은 초기화됨

## 성능 비교

### 메모리 모드
- 장점: 빠른 속도, 복잡한 설정 불필요
- 단점: 서버 재시작 시 데이터 손실, 스케일아웃 불가

### Redis 모드  
- 장점: 데이터 지속성, 스케일아웃 가능, 서버 간 공유
- 단점: 네트워크 지연, Redis 서버 의존성, 설정 복잡

## 문제 해결

### 자주 발생하는 문제
1. **직렬화 에러**: 객체 구조 변경 시 발생 → Redis flush 필요
2. **연결 실패**: Redis 서버 상태 확인 필요
3. **TTL 만료**: 게임 중 데이터 사라짐 → TTL 연장 로직 확인

### 디버깅 팁
```java
// Repository에서 직접 확인
@Autowired RedisRoomRepository repository;

// 활성 방 개수
long count = repository.countActiveRooms();

// TTL 연장
repository.extendRoomTtl(joinCode, Duration.ofHours(2));
```
