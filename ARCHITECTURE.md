# Coffee Shout 백엔드 아키텍처

> 이 문서는 `claude/github-be-setup-011CV5P7bJAk33XEG7gFY1jX` 브랜치의 백엔드 아키텍처를 설명합니다.

## 목차
1. [전체 아키텍처](#전체-아키텍처)
2. [계층별 상세 설명](#계층별-상세-설명)
3. [이벤트 기반 아키텍처](#이벤트-기반-아키텍처)
4. [데이터 흐름](#데이터-흐름)
5. [WebSocket 통신](#websocket-통신)
6. [Redis 활용](#redis-활용)
7. [동시성 제어](#동시성-제어)
8. [설계 원칙](#설계-원칙)

---

## 전체 아키텍처

### 시스템 구성도

```
┌────────────────────────────────────────────────────────────────┐
│                         Frontend                                │
│                   (React/Vue/Angular)                           │
└──────────────────┬──────────────────────┬──────────────────────┘
                   │ REST API             │ WebSocket (STOMP)
                   ▼                      ▼
┌──────────────────────────────────────────────────────────────────┐
│                      Spring Boot Backend                          │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    UI Layer                                 │ │
│  │  - REST Controllers (@RestController)                      │ │
│  │  - WebSocket Controllers (@MessageMapping)                 │ │
│  │  - Request/Response DTOs                                   │ │
│  │  - MessagePublisher (WebSocket 메시지 전송)                │ │
│  └────────────────┬────────────────────────────────────────────┘ │
│                   │                                               │
│  ┌────────────────▼────────────────────────────────────────────┐ │
│  │                Application Layer                            │ │
│  │  - Application Services (@Service)                         │ │
│  │  - Use Case 조율                                           │ │
│  │  - 트랜잭션 경계 (@Transactional)                          │ │
│  │  - 이벤트 발행                                             │ │
│  └────────────────┬────────────────────────────────────────────┘ │
│                   │                                               │
│  ┌────────────────▼────────────────────────────────────────────┐ │
│  │                   Domain Layer                              │ │
│  │  - Domain Models (Entities, Value Objects)                 │ │
│  │  - Domain Services (CommandService, QueryService)          │ │
│  │  - Domain Events                                           │ │
│  │  - Business Logic                                          │ │
│  │  - Repository Interfaces                                   │ │
│  └────────────────┬────────────────────────────────────────────┘ │
│                   │                                               │
│  ┌────────────────▼────────────────────────────────────────────┐ │
│  │              Infrastructure Layer                           │ │
│  │  - JPA Repositories (@Repository)                          │ │
│  │  - Redis Event Publishers                                  │ │
│  │  - Redis Event Subscribers                                 │ │
│  │  - Redis Stream Handlers                                   │ │
│  │  - External Services (S3, QR 생성)                         │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
└──────┬────────────────────────────┬────────────────────┬─────────┘
       │                            │                    │
       ▼                            ▼                    ▼
┌─────────────┐           ┌──────────────┐      ┌──────────────┐
│   MySQL     │           │    Redis     │      │   AWS S3     │
│             │           │ - Pub/Sub    │      │ - QR Codes   │
│ - Room      │           │ - Streams    │      │              │
│ - Player    │           │ - Lock       │      └──────────────┘
│ - MiniGame  │           │ - Session    │
│ - Results   │           └──────────────┘
└─────────────┘
```

---

## 계층별 상세 설명

### 1. UI Layer (Presentation Layer)

**책임**:
- HTTP 요청/응답 처리
- WebSocket 메시지 송수신
- 입력 검증 (간단한 형식 검증)
- DTO 변환 (도메인 ↔ DTO)

**패키지 구조**:
```
ui/
├── RoomRestController.java           # REST API
├── RoomWebSocketController.java      # WebSocket API
├── messaging/
│   └── RoomMessagePublisher.java     # WebSocket 메시지 발행
├── request/
│   ├── RoomCreateRequest.java
│   └── RoomEnterRequest.java
└── response/
    ├── RoomStatusResponse.java
    └── PlayerResponse.java
```

**예시**:
```java
@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {
    private final RoomService roomService;

    @PostMapping
    public RoomCreateResponse createRoom(@RequestBody RoomCreateRequest request) {
        // 1. 요청 DTO → 도메인 객체
        // 2. Application Service 호출
        // 3. 결과 → 응답 DTO
        return roomService.createRoom(request.toCommand());
    }
}

@Controller
public class RoomWebSocketController {
    private final MiniGameCommandDispatcher commandDispatcher;

    @MessageMapping("/room/{roomId}/minigame")
    public void handleMiniGameCommand(
        @DestinationVariable String roomId,
        @Payload MiniGameMessage message,
        StompHeaderAccessor accessor
    ) {
        String playerName = accessor.getUser().getName();
        commandDispatcher.dispatch(roomId, playerName, message);
    }
}
```

**주의사항**:
- UI Layer는 비즈니스 로직을 포함하지 않음
- 도메인 모델을 직접 반환하지 않음 (DTO 사용)
- MessagePublisher는 UI Layer에 위치 (Spring Event 처리 후 WebSocket 메시지 전송)

---

### 2. Application Layer

**책임**:
- Use Case 조율 (여러 도메인 서비스 조합)
- 트랜잭션 경계 관리 (`@Transactional`)
- 도메인 이벤트 → Spring Event 변환
- 애플리케이션 수준의 검증

**패키지 구조**:
```
application/
├── RoomService.java                  # Room 관련 Use Case
├── MiniGameService.java              # 미니게임 관련 Use Case
└── initializer/
    ├── DataInitializer.java          # 초기 데이터 로드
    └── MenuInitializer.java
```

**예시**:
```java
@Service
@Transactional
public class RoomService {
    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;
    private final JoinCodeGenerator joinCodeGenerator;
    private final QrCodeService qrCodeService;
    private final RoomEventPublisher eventPublisher;

    public RoomCreateResponse createRoom(String hostName, Long menuId) {
        // 1. 도메인 서비스를 통해 방 생성
        JoinCode joinCode = joinCodeGenerator.generate();
        Room room = roomCommandService.createRoom(joinCode, hostName, menuId);

        // 2. QR 코드 생성
        QrCode qrCode = qrCodeService.generateQrCode(joinCode);

        // 3. 이벤트 발행
        eventPublisher.publish(new RoomCreateEvent(room, qrCode));

        // 4. 응답 반환
        return RoomCreateResponse.from(room, qrCode);
    }
}
```

**특징**:
- 여러 도메인 서비스를 조합하여 Use Case 구현
- 트랜잭션 경계는 Application Service 메서드
- 도메인 이벤트를 받아서 외부 시스템(Redis, WebSocket)으로 전파

---

### 3. Domain Layer

**책임**:
- 비즈니스 로직 구현
- 도메인 규칙 검증
- 도메인 이벤트 발행
- 영속성에 무관한 순수한 도메인 모델

**패키지 구조**:
```
domain/
├── Room.java                         # 집합 루트 (Aggregate Root)
├── Player.java                       # 엔티티
├── Roulette.java                     # 엔티티
├── JoinCode.java                     # 값 객체 (Value Object)
├── event/                            # 도메인 이벤트
│   ├── RoomCreateEvent.java
│   ├── PlayerKickEvent.java
│   └── RouletteSpinEvent.java
├── repository/                       # 레포지토리 인터페이스
│   ├── RoomRepository.java
│   └── JoinCodeRepository.java
└── service/                          # 도메인 서비스
    ├── RoomCommandService.java
    ├── RoomQueryService.java
    └── JoinCodeGenerator.java
```

**도메인 모델 예시**:
```java
@Getter
public class Room {
    private final String id;
    private final JoinCode joinCode;
    private Player host;
    private Players players;
    private Roulette roulette;
    private RoomState state;

    // 비즈니스 로직
    public void joinGuest(Player guest) {
        validateCanJoin();
        validateNoDuplicateName(guest.getName());

        players.add(guest);
        roulette.addPlayer(guest);
    }

    public void startMiniGame(Player requester, MiniGameType type) {
        validateIsHost(requester);
        validateCanStartGame();

        // 상태 변경
        this.state = RoomState.PLAYING;

        // 도메인 이벤트 발행은 Application Layer에서 처리
    }

    private void validateCanJoin() {
        if (state != RoomState.READY) {
            throw new InvalidStateException("게임 중에는 입장할 수 없습니다.");
        }
        if (players.isFull()) {
            throw new InvalidStateException("방이 가득 찼습니다.");
        }
    }
}
```

**도메인 서비스 예시**:
```java
@Component
public class RoomCommandService {
    private final RoomRepository roomRepository;
    private final MenuQueryService menuQueryService;

    public Room createRoom(JoinCode joinCode, String hostName, Long menuId) {
        Menu menu = menuQueryService.findById(menuId);
        Player host = Player.createHost(hostName, menu);

        Room room = Room.create(joinCode, host);
        return roomRepository.save(room);
    }

    public void joinGuest(String joinCode, String guestName, Long menuId) {
        Room room = roomRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new NotExistElementException("존재하지 않는 방입니다."));

        Menu menu = menuQueryService.findById(menuId);
        Player guest = Player.createGuest(guestName, menu);

        room.joinGuest(guest);
        roomRepository.save(room);
    }
}
```

**특징**:
- 도메인 모델은 영속성 기술(JPA)과 독립적으로 설계
- 비즈니스 규칙은 도메인 객체 내부에 캡슐화
- 도메인 서비스는 여러 도메인 객체를 조율하는 로직 담당

---

### 4. Infrastructure Layer

**책임**:
- 도메인 레포지토리 구현 (JPA)
- 외부 시스템 연동 (Redis, S3)
- 이벤트 발행/구독 구현
- 메시징 처리 (Redis Pub/Sub, Streams)

**패키지 구조**:
```
infra/
├── persistence/                      # 영속성
│   ├── RoomEntity.java              # JPA 엔티티
│   ├── RoomJpaRepository.java       # JPA 레포지토리
│   └── RoomPersistenceService.java  # 영속성 서비스
├── messaging/                        # 메시징
│   ├── RedisRoomEventPublisher.java # 이벤트 발행
│   ├── RoomEventSubscriber.java     # 이벤트 구독
│   └── handler/                     # 이벤트 핸들러
│       ├── RoomCreateEventHandler.java
│       └── PlayerKickEventHandler.java
├── RedisJoinCodeRepository.java     # Redis 레포지토리
├── S3Service.java                   # S3 연동
└── ZxingQrCodeGenerator.java        # QR 코드 생성
```

**JPA 엔티티 예시**:
```java
@Entity
@Table(name = "rooms")
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String joinCode;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<PlayerEntity> players = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RoomState state;

    // 도메인 모델로 변환
    public Room toDomain() {
        // ...
    }

    // 도메인 모델로부터 생성
    public static RoomEntity from(Room room) {
        // ...
    }
}
```

**Redis 이벤트 발행**:
```java
@Component
public class RedisRoomEventPublisher implements RoomEventPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TopicManager topicManager;

    @Override
    public void publish(RoomBaseEvent event) {
        String topic = topicManager.getTopicName(event.getRoomId(), event.getEventType());
        redisTemplate.convertAndSend(topic, event);
    }
}
```

**Redis 이벤트 구독**:
```java
@Component
public class RoomEventSubscriber implements EventSubscriber {
    private final RoomEventHandlerFactory handlerFactory;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RoomBaseEvent event = deserialize(message.getBody());
        RoomEventHandler handler = handlerFactory.getHandler(event.getEventType());
        handler.handle(event);
    }
}
```

---

## 이벤트 기반 아키텍처

### 이벤트 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│                          요청 처리                               │
└─────────────────────────────────────────────────────────────────┘

1. UI Layer: HTTP/WebSocket 요청 수신
              ↓
2. Application Layer: Use Case 실행 + 트랜잭션
              ↓
3. Domain Layer: 비즈니스 로직 실행
              ↓
4. Application Layer: 도메인 이벤트 → Spring Event 발행
              ↓
5. Infrastructure Layer: Redis Pub/Sub으로 이벤트 전파
              ↓
6. Infrastructure Layer: 다른 서버에서 이벤트 수신
              ↓
7. Application Layer: Spring Event 처리
              ↓
8. UI Layer: WebSocket 메시지 브로드캐스트

┌─────────────────────────────────────────────────────────────────┐
│                       이벤트 타입                                │
└─────────────────────────────────────────────────────────────────┘

1. 도메인 이벤트 (Domain Event)
   - 도메인 로직 실행 결과
   - 예: "방이 생성됨", "플레이어가 강퇴됨"
   - 위치: domain/event/

2. Spring Event (Application Event)
   - 애플리케이션 내부 이벤트
   - Spring의 @EventListener로 처리
   - 예: @EventListener handle(PlayerKickEvent)

3. 브로드캐스트 이벤트 (Broadcast Event)
   - Redis Pub/Sub으로 전파
   - 다른 서버 인스턴스로 전달
   - 예: PlayerListChangedEvent
   - 위치: domain/event/broadcast/

4. Command Event
   - 명령 실행 요청
   - Redis Stream으로 처리
   - 예: TapCommandEvent, SelectCardCommandEvent
```

### 이벤트 예시

#### 1. 플레이어 강퇴 시나리오

```java
// Step 1: UI Layer - 요청 수신
@MessageMapping("/room/{roomId}/kick")
public void kickPlayer(@DestinationVariable String roomId,
                       @Payload KickMessage message) {
    roomPlayerService.kickPlayer(roomId, message.getTargetPlayerName());
}

// Step 2: Application Layer - Use Case 실행
@Transactional
public void kickPlayer(String roomId, String targetPlayerName) {
    // 도메인 서비스 호출
    playerCommandService.kick(roomId, targetPlayerName);

    // Spring Event 발행
    applicationEventPublisher.publishEvent(
        new PlayerKickEvent(roomId, targetPlayerName)
    );
}

// Step 3: Infrastructure Layer - Redis 전파
@EventListener
@Async
public void handle(PlayerKickEvent event) {
    // Redis Pub/Sub으로 다른 서버에 전파
    redisTemplate.convertAndSend(
        "room:" + event.getRoomId() + ":player:kick",
        event
    );
}

// Step 4: Infrastructure Layer - Redis 이벤트 수신 (다른 서버)
@Override
public void onMessage(Message message, byte[] pattern) {
    PlayerKickEvent event = deserialize(message.getBody());

    // Spring Event 재발행
    applicationEventPublisher.publishEvent(
        new PlayerListChangedEvent(event.getRoomId(), ...)
    );
}

// Step 5: UI Layer - WebSocket 브로드캐스트
@EventListener
public void handle(PlayerListChangedEvent event) {
    // WebSocket으로 클라이언트에 전송
    roomMessagePublisher.publishPlayerListChanged(
        event.getRoomId(),
        event.getPlayers()
    );
}
```

### 이벤트 네이밍 규칙

| 유형 | 네이밍 | 예시 |
|------|--------|------|
| 도메인 이벤트 | `{명사}{동사}Event` | `PlayerKickEvent` |
| 브로드캐스트 이벤트 | `{명사}{동사 과거형}Event` | `PlayerListChangedEvent` |
| Command 이벤트 | `{동사}{명사}CommandEvent` | `SelectCardCommandEvent` |

---

## 데이터 흐름

### REST API 흐름

```
Client Request
      ↓
┌─────────────────┐
│ REST Controller │ UI Layer
└────────┬────────┘
         ↓ RoomCreateRequest
┌─────────────────┐
│ RoomService     │ Application Layer
│ @Transactional  │
└────────┬────────┘
         ↓
┌──────────────────┐
│RoomCommandService│ Domain Layer
└────────┬─────────┘
         ↓
┌─────────────────┐
│ RoomRepository  │ Infrastructure Layer
└────────┬────────┘
         ↓
┌─────────────────┐
│ MySQL (rooms)   │ Database
└─────────────────┘

Response Flow (역순)
Room → RoomCreateResponse → Client
```

### WebSocket 메시지 흐름

```
Client Message (STOMP)
      ↓
┌──────────────────────┐
│WebSocket Controller  │ UI Layer
└──────────┬───────────┘
           ↓ MiniGameMessage
┌──────────────────────┐
│CommandDispatcher     │ UI Layer
└──────────┬───────────┘
           ↓ SelectCardCommand
┌──────────────────────┐
│SelectCardHandler     │ UI Layer
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│CardGameService       │ Application Layer
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│CardGameCommandService│ Domain Layer
└──────────┬───────────┘
           ↓ SelectCardCommandEvent
┌──────────────────────┐
│Redis Stream Producer │ Infrastructure Layer
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│Redis Streams         │ Redis
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│Redis Stream Consumer │ Infrastructure Layer
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│EventHandler          │ Infrastructure Layer
└──────────┬───────────┘
           ↓
┌──────────────────────┐
│CardGame (Domain)     │ Domain Layer
└──────────┬───────────┘
           ↓ CardSelectedEvent (Spring Event)
┌──────────────────────┐
│MessagePublisher      │ UI Layer
└──────────┬───────────┘
           ↓ STOMP Message
┌──────────────────────┐
│All Clients (Broadcast)│
└──────────────────────┘
```

---

## WebSocket 통신

### STOMP 엔드포인트 구조

```
ws://domain/ws
│
├─ /app/room/{roomId}/...           # Client → Server (Message)
│   ├─ /ready                        # 준비 상태 변경
│   ├─ /menu                         # 메뉴 선택
│   ├─ /minigame/select              # 미니게임 선택
│   ├─ /minigame                     # 미니게임 명령
│   └─ /roulette/spin                # 룰렛 돌리기
│
└─ /topic/room/{roomId}/...         # Server → Client (Subscribe)
    ├─ /players                      # 플레이어 목록 변경
    ├─ /minigame/list                # 미니게임 목록 변경
    ├─ /minigame/state               # 미니게임 상태 변경
    ├─ /roulette/show                # 룰렛 표시
    └─ /roulette/result              # 룰렛 결과
```

### 세션 관리

```java
@Component
public class StompSessionManager {
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String playerName, String roomId) {
        sessions.put(sessionId, new SessionInfo(playerName, roomId));

        // Redis Pub/Sub으로 다른 서버에 전파
        eventPublisher.publish(new SessionRegisteredEvent(sessionId, playerName, roomId));
    }

    public void removeSession(String sessionId) {
        SessionInfo info = sessions.remove(sessionId);

        if (info != null) {
            // 플레이어 연결 해제 이벤트 발행
            eventPublisher.publish(new PlayerDisconnectedEvent(info.getRoomId(), info.getPlayerName()));
        }
    }
}
```

### 재연결 처리

```java
@Component
public class DelayedPlayerRemovalService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void scheduleRemoval(String roomId, String playerName) {
        // 10초 후 플레이어 제거 스케줄링
        ScheduledFuture<?> future = scheduler.schedule(
            () -> playerRemovalService.removePlayer(roomId, playerName),
            10,
            TimeUnit.SECONDS
        );

        // 재연결 시 취소 가능하도록 저장
        scheduledRemovals.put(playerName, future);
    }

    public void cancelRemoval(String playerName) {
        ScheduledFuture<?> future = scheduledRemovals.remove(playerName);
        if (future != null) {
            future.cancel(false);
        }
    }
}
```

---

## Redis 활용

### 1. Pub/Sub (이벤트 브로드캐스트)

**목적**: 여러 서버 인스턴스 간 이벤트 전파

```java
// 발행
redisTemplate.convertAndSend("room:ABC123:player:list", event);

// 구독
@Component
public class RoomEventSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 이벤트 처리
    }
}
```

**토픽 네이밍 규칙**:
```
{도메인}:{ID}:{이벤트타입}

예시:
- room:ABC123:player:list
- room:ABC123:minigame:select
- room:ABC123:roulette:spin
```

### 2. Streams (명령 처리)

**목적**: 순서 보장이 필요한 명령 처리

```java
// Producer
@Component
public class CardSelectStreamProducer {
    public void produce(String roomId, SelectCardCommandEvent event) {
        String streamKey = "stream:cardgame:" + roomId;
        redisTemplate.opsForStream().add(streamKey, event.toMap());
    }
}

// Consumer
@Component
public class CardSelectStreamHandler implements StreamListener<String, MapRecord<String, String, String>> {
    @Override
    @StreamListener(streams = "stream:cardgame:*", autoStartup = "true")
    public void onMessage(MapRecord<String, String, String> message) {
        SelectCardCommandEvent event = deserialize(message);
        eventHandler.handle(event);
    }
}
```

### 3. Distributed Lock (동시성 제어)

**목적**: 분산 환경에서 동시성 제어

```java
@Component
public class RoomCommandService {

    @RedisLock(key = "'room:' + #joinCode", waitTime = 3, leaseTime = 5)
    public void joinGuest(String joinCode, String guestName, Long menuId) {
        // 동시에 여러 게스트가 입장 시도해도 순차적으로 처리됨
        Room room = roomRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new NotExistElementException("존재하지 않는 방입니다."));

        room.joinGuest(guest);
        roomRepository.save(room);
    }
}

// AOP로 구현
@Aspect
@Component
public class RedisLockAspect {
    private final RedissonClient redissonClient;

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String key = resolveKey(redisLock.key(), joinPoint);
        RLock lock = redissonClient.getLock(key);

        try {
            boolean acquired = lock.tryLock(redisLock.waitTime(), redisLock.leaseTime(), TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("락 획득 실패");
            }

            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

### 4. Session Storage (초대 코드)

**목적**: JoinCode → RoomId 매핑

```java
@Component
public class RedisJoinCodeRepository implements JoinCodeRepository {
    private static final String KEY_PREFIX = "joincode:";
    private static final Duration TTL = Duration.ofHours(24);

    @Override
    public void save(JoinCode joinCode, String roomId) {
        String key = KEY_PREFIX + joinCode.getValue();
        redisTemplate.opsForValue().set(key, roomId, TTL);
    }

    @Override
    public Optional<String> findRoomId(JoinCode joinCode) {
        String key = KEY_PREFIX + joinCode.getValue();
        String roomId = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(roomId);
    }
}
```

---

## 동시성 제어

### 1. 낙관적 락 (Optimistic Lock)

**사용 사례**: 충돌이 드문 경우

```java
@Entity
public class RoomEntity {
    @Version
    private Long version;

    // ...
}
```

**장점**: 락 대기 없음, 성능 좋음
**단점**: 충돌 시 재시도 필요

### 2. 비관적 락 (Pessimistic Lock)

**사용 사례**: 충돌이 빈번한 경우

```java
@Query("SELECT r FROM RoomEntity r WHERE r.joinCode = :joinCode")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<RoomEntity> findByJoinCodeWithLock(@Param("joinCode") String joinCode);
```

**장점**: 데이터 정합성 보장
**단점**: 락 대기 시간 발생 가능

### 3. 분산 락 (Distributed Lock)

**사용 사례**: 여러 서버에서 동시 접근 제어

```java
@RedisLock(key = "'room:' + #roomId", waitTime = 3, leaseTime = 5)
public void startMiniGame(String roomId, MiniGameType type) {
    // ...
}
```

**장점**: 분산 환경에서도 동시성 보장
**단점**: Redis 의존성, 약간의 오버헤드

### 4. Redis Streams (순서 보장)

**사용 사례**: 이벤트 순서가 중요한 경우 (카드 선택, 탭 등)

```java
// Consumer Group 사용
@StreamListener(
    streams = "stream:cardgame:*",
    consumerGroup = "cardgame-handlers"
)
public void onMessage(MapRecord<String, String, String> message) {
    // 메시지는 순서대로 처리됨
}
```

**장점**: 순서 보장, 메시지 손실 방지
**단점**: 설정 복잡도 증가

---

## 설계 원칙

### 1. 계층 의존성 규칙

```
UI Layer
   ↓ (의존)
Application Layer
   ↓ (의존)
Domain Layer
   ↑ (구현)
Infrastructure Layer
```

**규칙**:
- 상위 계층은 하위 계층에 의존 가능
- 하위 계층은 상위 계층에 의존 불가
- Domain Layer는 어떤 계층에도 의존하지 않음 (순수 Java)
- Infrastructure Layer는 Domain Layer의 인터페이스를 구현

### 2. 도메인 주도 설계 (DDD)

**Aggregate (집합체)**:
- Room은 Aggregate Root
- Player, Roulette는 Room의 일부
- Room을 통해서만 Player와 Roulette 접근 가능

**Repository**:
- Aggregate Root 단위로만 레포지토리 존재
- RoomRepository는 있지만 PlayerRepository는 없음

**Value Object**:
- JoinCode, PlayerName, Probability 등
- 불변 객체
- 값으로 비교 (equals/hashCode)

### 3. 단일 책임 원칙 (SRP)

각 계층은 하나의 책임만:
- UI Layer: 입출력 처리
- Application Layer: Use Case 조율
- Domain Layer: 비즈니스 로직
- Infrastructure Layer: 기술 구현

### 4. 의존성 역전 원칙 (DIP)

Domain Layer는 추상화에 의존:
```java
// Domain Layer
public interface RoomRepository {
    Room save(Room room);
    Optional<Room> findById(String id);
}

// Infrastructure Layer
@Repository
public class JpaRoomRepository implements RoomRepository {
    // JPA 구현
}
```

### 5. 이벤트 기반 통신

모듈 간 직접 의존 대신 이벤트로 통신:
- 결합도 낮춤
- 확장성 향상
- 테스트 용이성

---

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Domain-Driven Design (DDD)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Redis Pub/Sub](https://redis.io/topics/pubsub)
- [Redis Streams](https://redis.io/topics/streams-intro)
- [STOMP Protocol](https://stomp.github.io/)

---

**작성일**: 2025-11-21
**마지막 업데이트**: 2025-11-21
