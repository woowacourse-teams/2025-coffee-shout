# 백엔드 리팩토링 변경사항 (Phase 4)

> 커밋 `1385646` 이후 진행된 계층 구조 리팩토링에 대한 설명입니다.

## 📋 목차
1. [개요](#개요)
2. [변경의 특징](#변경의-특징)
3. [장단점 분석](#장단점-분석)
4. [개선 방향](#개선-방향)
5. [참고 커밋](#참고-커밋)

---

## 개요

**Phase 4: 계층 구조 리팩토링**은 6개의 주요 커밋으로 진행되었습니다:

| 순서 | 커밋 | 내용 |
|------|------|------|
| 1 | `bb99797` | Service를 Domain Layer로 이동 (Application Layer 제거) |
| 2 | `0925325` | Application Layer 재도입 + Domain Service 네이밍 개선 |
| 3 | `acd0d40` | STOMP 브로드캐스트 → Spring Event 패턴 전환 |
| 4 | `f54e59f` | MessagePublisher를 Infrastructure → UI Layer 이동 |
| 5 | `7ab2f44` | ApplicationService → Service 네이밍 통일 |
| 6 | `8261197` | 테스트 구조 정리 + LogAspect 수정 |

---

## 변경의 특징

### 1️⃣ 계층 구조의 명확화

#### Before (bb99797 이전)
```
Controller → ??? → Domain
```
- Application Layer의 역할이 불명확
- Handler가 어느 계층 Service를 호출해야 하는지 혼란

#### After (0925325)
```
Controller → Application Service → Domain Service (CommandService)
Handler → Domain Service (직접 호출)
```

**변경된 구조:**
```
room/
├── ui/                              # UI Layer
│   ├── RoomRestController.java
│   ├── RoomWebSocketController.java
│   └── messaging/
│       └── RoomMessagePublisher.java
│
├── application/                     # Application Layer
│   └── service/
│       ├── RoomService.java         # Use Case 조율
│       └── RoomPlayerService.java
│
├── domain/                          # Domain Layer
│   ├── Room.java
│   ├── Player.java
│   ├── repository/
│   └── service/
│       ├── PlayerCommandService.java # 도메인 로직
│       └── RoomQueryService.java
│
└── infra/                           # Infrastructure Layer
    ├── persistence/
    └── messaging/
```

**특징:**
- **Controller**: Application Service 사용
- **Handler (Infrastructure)**: Domain Service 직접 사용
- **명확한 역할 분리**: 각 계층의 책임이 명확함

---

### 2️⃣ 이벤트 기반 통신 (acd0d40)

#### Before
```java
// Handler에서 직접 WebSocket 메시지 전송
@Component
class PlayerKickEventHandler {
    private final LoggingSimpMessagingTemplate messagingTemplate;

    void handle(PlayerKickEvent event) {
        // 비즈니스 로직
        messagingTemplate.convertAndSend("/topic/...", message); // ❌
    }
}
```

#### After
```java
// 1. Handler: Spring Event 발행
@Component
class PlayerKickEventHandler {
    private final ApplicationEventPublisher eventPublisher;

    void handle(PlayerKickEvent event) {
        // 비즈니스 로직
        eventPublisher.publishEvent(new PlayerListChangedEvent(...)); // ✅
    }
}

// 2. MessagePublisher (UI Layer): Event 수신 후 WebSocket 전송
@Component
class RoomMessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    void handle(PlayerListChangedEvent event) {
        messagingTemplate.convertAndSend("/topic/...", message);
    }
}
```

**이벤트 흐름:**
```
Handler (Infrastructure)
  → Spring Event 발행
    → MessagePublisher (UI)
      → WebSocket 브로드캐스트
```

**특징:**
- Handler는 WebSocket 기술을 몰라도 됨
- 관심사 분리 (비즈니스 로직 vs 메시지 전송)
- MessagePublisher 교체 가능 (WebSocket → SSE 등)

---

### 3️⃣ 네이밍 규칙 통일 (7ab2f44)

#### Application Layer
- `RoomPlayerService`, `RoomMiniGameService`
- **"Service"** 접미사 사용
- Use Case 조율 담당

#### Domain Layer
- `PlayerCommandService`, `MiniGameCommandService`
- **"CommandService"** 접미사 사용
- 도메인 로직 실행 담당

**예시:**
```java
// Application Layer
@Service
class RoomPlayerService {
    private final PlayerCommandService playerCommandService; // Domain

    @Transactional
    public void kickPlayer(String roomId, String playerName) {
        playerCommandService.kick(roomId, playerName);
        // 추가 Use Case 로직...
    }
}

// Domain Layer
@Component
class PlayerCommandService {
    private final RoomRepository roomRepository;

    public void kick(String roomId, String playerName) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NotExistElementException("방을 찾을 수 없습니다."));

        room.kickPlayer(playerName); // 도메인 로직
        roomRepository.save(room);
    }
}
```

---

## 장단점 분석

### ✅ 장점

#### 1. 계층별 책임이 명확해짐

**Before:**
```java
// Handler에서 뭘 호출해야 할지 애매
playerDisconnectionService.handle() {
    roomService.removePlayer(); // 이게 Application? Domain?
}
```

**After:**
```java
// Controller → Application Service
roomRestController.kickPlayer() {
    roomPlayerService.kickPlayer(); // Application
}

// Handler → Domain Service (직접)
playerDisconnectionService.handle() {
    playerCommandService.kick(); // Domain
}
```

**효과:**
- Controller는 Application Service만 사용
- Infrastructure(Handler)는 Domain Service 직접 사용
- 각자 명확한 역할

---

#### 2. 이벤트 기반 통신으로 결합도 감소

**Before:**
```java
// Handler가 WebSocket에 직접 의존
@Component
class PlayerKickEventHandler {
    private final LoggingSimpMessagingTemplate messagingTemplate; // ❌

    void handle() {
        // 비즈니스 로직
        messagingTemplate.convertAndSend(...); // UI 기술에 의존
    }
}
```

**After:**
```java
// Handler: Event만 발행
@Component
class PlayerKickEventHandler {
    private final ApplicationEventPublisher eventPublisher; // ✅

    void handle() {
        // 비즈니스 로직
        eventPublisher.publishEvent(new PlayerListChangedEvent(...));
    }
}
```

**효과:**
- Handler는 WebSocket 기술 몰라도 됨
- MessagePublisher 교체 가능
- 테스트 시 이벤트 발행만 검증

---

#### 3. 네이밍으로 역할 구분 가능

```
Application Layer: RoomPlayerService, RoomMiniGameService
                   → Use Case 조율

Domain Layer: PlayerCommandService, MiniGameCommandService
              → 도메인 로직 실행
```

**효과:**
- 코드만 봐도 어느 계층인지 알 수 있음
- `CommandService` = 도메인 로직 변경
- `Service` = Use Case 조율

---

### ⚠️ 단점

#### 1. 계층이 늘어나서 복잡도 증가

**간단한 기능도 여러 계층을 거침:**
```
Controller (요청 수신)
  ↓
Application Service (조율)
  ↓
Domain Service (비즈니스 로직)
  ↓
Repository (저장)
```

**예시:**
```java
// 단순히 플레이어 강퇴하는데도...
RoomRestController.kickPlayer()
  → RoomPlayerService.kickPlayer()        // Application
    → PlayerCommandService.kick()          // Domain
      → roomRepository.save()              // Infra
```

**문제점:**
- 간단한 CRUD도 3개 파일 수정
- 호출 스택이 깊어짐
- 신규 개발자 진입장벽

---

#### 2. 이벤트 흐름 추적이 어려움

**코드 흐름:**
```
Handler
  → eventPublisher.publishEvent(PlayerKickEvent)
    → [Spring Event]
      → RedisEventPublisher (Redis 전파)
        → [Redis Pub/Sub]
          → RoomEventSubscriber (다른 서버)
            → eventPublisher.publishEvent(PlayerListChangedEvent)
              → [Spring Event]
                → MessagePublisher
                  → WebSocket 브로드캐스트
```

**문제점:**
- 디버깅 시 Event 흐름 따라가기 힘듦
- IDE로 호출 추적 안 됨 (@EventListener는 런타임)
- 어디서 Event 발행하고 누가 받는지 파악 어려움

---

#### 3. Application Service의 역할이 애매한 경우 존재

```java
// 이 Service는 단순 위임만 함
@Service
class RoomPlayerService {
    private final PlayerCommandService playerCommandService;

    public void kickPlayer(String roomId, String playerName) {
        playerCommandService.kick(roomId, playerName); // 그냥 전달만
    }
}
```

**문제점:**
- 단순 위임만 하는 경우 Application Service 존재 이유 없음
- 보일러플레이트 코드 증가
- "이거 왜 만들었지?" 의문

---

## 개선 방향

### 개선 1: Application Service 통합 고려

**현재 문제:**
```java
// 단순 위임만 하는 Application Service
@Service
class RoomPlayerService {
    private final PlayerCommandService playerCommandService;

    public void kickPlayer(String roomId, String playerName) {
        playerCommandService.kick(roomId, playerName); // 그냥 전달만
    }
}
```

**개선 방안:**
```java
// 복잡한 Use Case만 Application Service로 분리
@Service
class RoomService {
    private final PlayerCommandService playerCommandService;
    private final RoomQueryService roomQueryService;
    private final QrCodeService qrCodeService;

    // ✅ 여러 Domain Service 조율하는 Use Case
    @Transactional
    public RoomCreateResponse createRoom(String hostName, Long menuId) {
        JoinCode joinCode = joinCodeGenerator.generate();
        Room room = roomCommandService.createRoom(joinCode, hostName, menuId);
        QrCode qrCode = qrCodeService.generateQrCode(joinCode);

        return RoomCreateResponse.from(room, qrCode);
    }
}
```

**개선 원칙:**
- ✅ **여러 Domain Service 조율** → Application Service 필요
- ✅ **단순 CRUD** → Controller에서 Domain Service 직접 호출
- 보일러플레이트 코드 감소

---

### 개선 2: 이벤트 흐름 가시성 향상

#### 방안 A: 이벤트 문서화

```java
/**
 * 플레이어 목록 변경 이벤트
 *
 * 발행 위치:
 * - PlayerKickEventHandler
 * - PlayerListUpdateEventHandler
 * - RoomJoinEventHandler
 *
 * 구독자:
 * - RoomMessagePublisher (WebSocket 브로드캐스트)
 */
public class PlayerListChangedEvent {
    // ...
}
```

#### 방안 B: 통합 테스트로 이벤트 흐름 검증

```java
@SpringBootTest
class EventFlowIntegrationTest {

    @Test
    void kickPlayer_shouldTriggerWebSocketBroadcast() {
        // Given
        CompletableFuture<PlayerListChangedEvent> future = new CompletableFuture<>();

        @EventListener
        void capture(PlayerListChangedEvent event) {
            future.complete(event);
        }

        // When
        playerKickEventHandler.handle(new PlayerKickEvent(...));

        // Then
        PlayerListChangedEvent event = future.get(3, TimeUnit.SECONDS);
        assertThat(event.getPlayers()).hasSize(1);
    }
}
```

---

### 개선 3: 계층 복잡도 완화 (Facade 패턴)

**간단한 기능은 Facade 패턴 활용:**

```java
@RestController
@RequiredArgsConstructor
class RoomRestController {
    private final RoomFacade roomFacade; // ✅ Facade

    @PostMapping
    public RoomCreateResponse createRoom(@RequestBody RoomCreateRequest request) {
        return roomFacade.createRoom(request); // 내부에서 여러 Service 조율
    }

    @DeleteMapping("/{roomId}/players/{playerName}")
    public void kickPlayer(@PathVariable String roomId,
                          @PathVariable String playerName) {
        roomFacade.kickPlayer(roomId, playerName);
    }
}

@Service
class RoomFacade {
    private final PlayerCommandService playerCommandService;
    private final MiniGameCommandService miniGameCommandService;

    // 복잡한 Use Case는 여러 Service 조율
    public RoomCreateResponse createRoom(RoomCreateRequest request) {
        // 여러 Domain Service 호출
    }

    // 간단한 작업은 Domain Service에 바로 위임
    public void kickPlayer(String roomId, String playerName) {
        playerCommandService.kick(roomId, playerName);
    }
}
```

---

### 개선 4: QueryService vs CommandService 분리 명확화

**개선 후:**
```java
// 조회 전용
@Component
class PlayerQueryService {
    public Player findByName(String roomId, String playerName) { }
    public List<Player> findAllByRoomId(String roomId) { }
}

// 변경 전용
@Component
class PlayerCommandService {
    @Transactional
    public void kick(String roomId, String playerName) { }

    @Transactional
    public void updateReadyState(String roomId, String playerName, boolean isReady) { }
}
```

**장점:**
- CQRS(Command Query Responsibility Segregation) 원칙
- 읽기/쓰기 성능 최적화 가능
- 트랜잭션 관리 명확

---

### 개선 5: 이벤트 네이밍 일관성

**개선 후:**
```java
// Command Event (명령)
class KickPlayerCommand { }
class SelectCardCommand { }

// Domain Event (결과)
class PlayerKickedEvent { }
class CardSelectedEvent { }

// Broadcast Event (UI 전파)
class PlayerListChangedEvent { }
class CardGameStateChangedEvent { }
```

**네이밍 규칙:**
- Command: `{동사}{명사}Command`
- Domain Event: `{명사}{동사과거형}Event`
- Broadcast Event: `{명사}{Changed/Updated}Event`

---

## 참고 커밋

### Phase 4 리팩토링 커밋 목록

```bash
# 1. Service를 Domain Layer로 이동
git show bb99797

# 2. Application Layer 재도입 + Domain Service 네이밍 개선
git show 0925325

# 3. STOMP 브로드캐스트 → Spring Event 패턴 전환
git show acd0d40

# 4. MessagePublisher를 UI Layer로 이동
git show f54e59f

# 5. ApplicationService → Service 네이밍 통일
git show 7ab2f44

# 6. 테스트 구조 정리 + LogAspect 수정
git show 8261197
```

### 전체 변경사항 확인

```bash
# 커밋 1385646 이후의 모든 리팩토링 확인
git log --oneline 1385646..HEAD --grep="refactor"
```

---

## 질문 & 피드백

이 리팩토링에 대한 질문이나 개선 아이디어가 있다면 팀 채널에 공유해주세요!

---

**작성일**: 2025-11-21
**작성자**: 백엔드 팀
