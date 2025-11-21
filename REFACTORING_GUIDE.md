# 리팩토링 가이드

> 이 문서는 `claude/github-be-setup-011CV5P7bJAk33XEG7gFY1jX` 브랜치에서 수행된 주요 리팩토링 작업을 설명합니다.

## 목차
1. [리팩토링 개요](#리팩토링-개요)
2. [계층 분리 및 네이밍 통일](#계층-분리-및-네이밍-통일)
3. [이벤트 기반 아키텍처 전환](#이벤트-기반-아키텍처-전환)
4. [MessagePublisher UI Layer 이동](#messagepublisher-ui-layer-이동)
5. [테스트 구조 개선](#테스트-구조-개선)
6. [패키지 구조 변경](#패키지-구조-변경)
7. [변경 이력](#변경-이력)

---

## 리팩토링 개요

### 목표
1. **명확한 계층 분리**: DDD 원칙에 따른 4-Layer Architecture 적용
2. **책임 분리**: 각 계층의 역할 명확화
3. **유지보수성 향상**: 코드 가독성 및 확장성 개선
4. **테스트 용이성**: 계층별 독립적 테스트 가능

### 주요 변경사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 계층 구조 | 불명확 | UI → Application → Domain → Infrastructure |
| 서비스 네이밍 | 혼재 (Service, ApplicationService 등) | 통일 (Application: Service, Domain: CommandService/QueryService) |
| 메시지 전송 | Service에서 직접 전송 | Event → EventHandler → MessagePublisher (UI) |
| 패키지 구조 | 평면적 | 계층별 명확한 구조 |

---

## 계층 분리 및 네이밍 통일

### 커밋
- `0925325`: Domain Service 네이밍 개선 및 Application Layer 도입으로 계층 분리
- `bb99797`: RoomPlayerService, RoomMiniGameService, RoomRouletteService를 Domain Service로 이동
- `7ab2f44`: ApplicationService 네이밍을 Service로 통일
- `d1d1bf0`: application 패키지 정리

### 변경 전: 모호한 계층 구조

```
room/
├── service/
│   ├── RoomService.java                    # 역할 불명확
│   ├── RoomPlayerService.java              # 역할 불명확
│   ├── RoomMiniGameService.java            # 역할 불명확
│   └── MenuService.java                    # 역할 불명확
├── domain/
│   ├── Room.java
│   └── Player.java
└── controller/
    └── RoomController.java
```

**문제점**:
1. Service의 역할이 불명확 (Application 로직인지 Domain 로직인지 구분 안 됨)
2. 트랜잭션 경계가 불명확
3. 도메인 로직과 Use Case 로직이 섞여 있음

### 변경 후: 명확한 4-Layer Architecture

```
room/
├── ui/                                      # UI Layer
│   ├── RoomRestController.java
│   ├── RoomWebSocketController.java
│   ├── messaging/
│   │   └── RoomMessagePublisher.java       # WebSocket 메시지 전송
│   ├── request/
│   └── response/
│
├── application/                             # Application Layer
│   └── service/
│       ├── RoomService.java                 # Room Use Case 조율
│       ├── RoomPlayerService.java           # Player Use Case 조율
│       ├── RoomMiniGameService.java         # MiniGame Use Case 조율
│       ├── RoomRouletteService.java         # Roulette Use Case 조율
│       └── MenuService.java                 # Menu Use Case 조율
│
├── domain/                                  # Domain Layer
│   ├── Room.java
│   ├── Player.java
│   ├── Roulette.java
│   ├── repository/
│   │   ├── RoomRepository.java             # 인터페이스
│   │   └── MenuRepository.java
│   └── service/                            # Domain Service
│       ├── RoomCommandService.java         # Room 생성/수정 로직
│       ├── RoomQueryService.java           # Room 조회 로직
│       ├── PlayerCommandService.java       # Player 생성/수정 로직
│       ├── MenuCommandService.java
│       └── MenuQueryService.java
│
└── infra/                                  # Infrastructure Layer
    ├── persistence/
    │   ├── RoomEntity.java
    │   ├── RoomJpaRepository.java
    │   └── RoomPersistenceService.java
    └── messaging/
        ├── RedisRoomEventPublisher.java
        └── RoomEventSubscriber.java
```

### 네이밍 규칙

#### Application Layer
```java
// Application Service: Use Case 조율
@Service
@Transactional
public class RoomService {  // ✅ "Service" 접미사
    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;

    public RoomCreateResponse createRoom(String hostName, Long menuId) {
        // 여러 도메인 서비스를 조합하여 Use Case 구현
    }
}
```

#### Domain Layer
```java
// Domain Command Service: 생성/수정 로직
@Component
public class RoomCommandService {  // ✅ "CommandService" 접미사
    private final RoomRepository roomRepository;

    public Room createRoom(JoinCode joinCode, Player host) {
        Room room = Room.create(joinCode, host);
        return roomRepository.save(room);
    }
}

// Domain Query Service: 조회 로직
@Component
public class RoomQueryService {  // ✅ "QueryService" 접미사
    private final RoomRepository roomRepository;

    public Room findByJoinCode(String joinCode) {
        return roomRepository.findByJoinCode(joinCode)
            .orElseThrow(() -> new NotExistElementException("존재하지 않는 방입니다."));
    }
}
```

#### Infrastructure Layer
```java
// Persistence Service: 영속성 처리
@Service
public class RoomPersistenceService {  // ✅ "PersistenceService" 접미사
    private final RoomJpaRepository roomJpaRepository;

    public void saveAll(List<Room> rooms) {
        List<RoomEntity> entities = rooms.stream()
            .map(RoomEntity::from)
            .toList();
        roomJpaRepository.saveAll(entities);
    }
}
```

### 계층별 책임

#### UI Layer
```java
@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {
    private final RoomService roomService;

    @PostMapping
    public RoomCreateResponse createRoom(@RequestBody RoomCreateRequest request) {
        // 책임: 요청 수신, DTO 변환, 응답 반환
        return roomService.createRoom(
            request.getHostName(),
            request.getMenuId()
        );
    }
}
```

#### Application Layer
```java
@Service
@Transactional
public class RoomService {
    private final RoomCommandService roomCommandService;
    private final JoinCodeGenerator joinCodeGenerator;
    private final QrCodeService qrCodeService;

    public RoomCreateResponse createRoom(String hostName, Long menuId) {
        // 책임: Use Case 조율, 트랜잭션 경계
        JoinCode joinCode = joinCodeGenerator.generate();
        Room room = roomCommandService.createRoom(joinCode, hostName, menuId);
        QrCode qrCode = qrCodeService.generateQrCode(joinCode);

        return RoomCreateResponse.from(room, qrCode);
    }
}
```

#### Domain Layer
```java
@Component
public class RoomCommandService {
    private final RoomRepository roomRepository;
    private final MenuQueryService menuQueryService;

    public Room createRoom(JoinCode joinCode, String hostName, Long menuId) {
        // 책임: 도메인 로직 실행
        Menu menu = menuQueryService.findById(menuId);
        Player host = Player.createHost(hostName, menu);

        Room room = Room.create(joinCode, host);
        return roomRepository.save(room);
    }
}
```

#### Infrastructure Layer
```java
@Component
public class JpaRoomRepository implements RoomRepository {
    private final RoomJpaRepository roomJpaRepository;

    @Override
    public Room save(Room room) {
        // 책임: 영속성 구현
        RoomEntity entity = RoomEntity.from(room);
        RoomEntity saved = roomJpaRepository.save(entity);
        return saved.toDomain();
    }
}
```

---

## 이벤트 기반 아키텍처 전환

### 커밋
- `acd0d40`: STOMP 브로드캐스트를 Spring Event 패턴으로 중앙화
- `d95beb8`: Event Handler들이 Domain Service를 사용하도록 수정

### 변경 전: Service에서 직접 WebSocket 메시지 전송

```java
@Service
public class RoomPlayerService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomRepository roomRepository;

    @Transactional
    public void kickPlayer(String roomId, String targetPlayerName) {
        // 1. 비즈니스 로직
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NotExistElementException("존재하지 않는 방입니다."));

        room.kickPlayer(targetPlayerName);
        roomRepository.save(room);

        // 2. WebSocket 메시지 직접 전송 (❌ 문제!)
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId + "/players",
            PlayerListMessage.from(room.getPlayers())
        );
    }
}
```

**문제점**:
1. **책임 분리 위반**: Service가 비즈니스 로직과 메시지 전송을 동시에 처리
2. **테스트 어려움**: WebSocket 의존성 때문에 Service 테스트가 복잡함
3. **확장성 부족**: 새로운 이벤트 핸들러 추가 시 Service 수정 필요
4. **분산 환경 문제**: 다른 서버 인스턴스에는 메시지가 전달되지 않음

### 변경 후: 이벤트 기반 아키텍처

#### Step 1: Application Service에서 Spring Event 발행

```java
@Service
@Transactional
public class RoomPlayerService {
    private final PlayerCommandService playerCommandService;
    private final ApplicationEventPublisher eventPublisher;  // ✅ Spring Event

    public void kickPlayer(String roomId, String targetPlayerName) {
        // 1. 비즈니스 로직만 처리
        playerCommandService.kick(roomId, targetPlayerName);

        // 2. Spring Event 발행
        Room room = roomQueryService.findById(roomId);
        eventPublisher.publishEvent(
            new PlayerKickEvent(roomId, targetPlayerName, room.getPlayers())
        );
    }
}
```

#### Step 2: Infrastructure Layer에서 Redis로 전파

```java
@Component
public class PlayerKickEventHandler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TopicManager topicManager;

    @EventListener
    @Async  // 비동기 처리
    public void handle(PlayerKickEvent event) {
        // Redis Pub/Sub으로 다른 서버 인스턴스에 전파
        String topic = topicManager.getTopicName(event.getRoomId(), RoomEventType.PLAYER_KICK);
        redisTemplate.convertAndSend(topic, event);
    }
}
```

#### Step 3: 다른 서버에서 이벤트 수신 및 Spring Event 재발행

```java
@Component
public class RoomEventSubscriber implements MessageListener {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Redis 메시지 수신
        PlayerKickEvent event = deserialize(message.getBody());

        // Spring Event 재발행 → UI Layer에서 처리
        eventPublisher.publishEvent(
            new PlayerListChangedEvent(event.getRoomId(), event.getPlayers())
        );
    }
}
```

#### Step 4: UI Layer에서 WebSocket 메시지 전송

```java
@Component
public class RoomMessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handle(PlayerListChangedEvent event) {
        // WebSocket으로 클라이언트에 브로드캐스트
        messagingTemplate.convertAndSend(
            "/topic/room/" + event.getRoomId() + "/players",
            PlayerListMessage.from(event.getPlayers())
        );
    }
}
```

### 이벤트 흐름 다이어그램

```
┌──────────────────────────────────────────────────────────────┐
│                      Server Instance 1                        │
│                                                               │
│  RoomPlayerService.kickPlayer()                              │
│         ↓                                                     │
│  PlayerCommandService.kick() (Domain Logic)                  │
│         ↓                                                     │
│  eventPublisher.publishEvent(PlayerKickEvent)                │
│         ↓                                                     │
│  PlayerKickEventHandler (Infrastructure)                     │
│         ↓                                                     │
│  redisTemplate.convertAndSend(topic, event)                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  Redis Pub/Sub  │
                  └────────┬─────────┘
                           │
         ┌─────────────────┴─────────────────┐
         │                                   │
         ▼                                   ▼
┌────────────────────┐            ┌────────────────────┐
│ Server Instance 1  │            │ Server Instance 2  │
│                    │            │                    │
│ RoomEventSubscriber│            │ RoomEventSubscriber│
│        ↓           │            │        ↓           │
│ PlayerListChanged  │            │ PlayerListChanged  │
│ Event              │            │ Event              │
│        ↓           │            │        ↓           │
│ RoomMessagePublisher│           │ RoomMessagePublisher│
│        ↓           │            │        ↓           │
│ WebSocket Broadcast│            │ WebSocket Broadcast│
└────────────────────┘            └────────────────────┘
```

### 장점

1. **관심사 분리**
   - Application Layer: 비즈니스 로직
   - Infrastructure Layer: 이벤트 전파
   - UI Layer: 메시지 전송

2. **테스트 용이성**
   ```java
   @Test
   void kickPlayer_shouldPublishEvent() {
       // Given
       String roomId = "room123";
       String targetPlayerName = "player2";

       // When
       roomPlayerService.kickPlayer(roomId, targetPlayerName);

       // Then
       verify(eventPublisher).publishEvent(any(PlayerKickEvent.class));  // ✅ 이벤트 발행만 검증
   }
   ```

3. **확장성**
   - 새로운 이벤트 핸들러 추가 용이
   - Service 코드 수정 불필요

4. **분산 환경 지원**
   - Redis Pub/Sub으로 모든 서버 인스턴스에 전파
   - 서버 추가/제거에 따른 코드 변경 없음

---

## MessagePublisher UI Layer 이동

### 커밋
- `f54e59f`: MessagePublisher를 UI Layer로 이동 및 API 문서 중앙화

### 변경 전: Service에 MessagePublisher 위치

```
room/
├── service/
│   ├── RoomService.java
│   └── RoomMessagePublisher.java      # ❌ Application Layer에 위치
└── controller/
    └── RoomController.java
```

**문제점**:
1. MessagePublisher가 Application Layer에 있으면 Service가 UI 계층 기술에 의존
2. WebSocket은 UI Layer의 기술인데 Application Layer에 노출됨

### 변경 후: UI Layer로 이동

```
room/
├── application/
│   └── service/
│       └── RoomService.java
└── ui/
    ├── RoomRestController.java
    ├── RoomWebSocketController.java
    └── messaging/
        └── RoomMessagePublisher.java  # ✅ UI Layer로 이동
```

**코드 예시**:

```java
// UI Layer
@Component
public class RoomMessagePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener  // Spring Event 수신
    public void handlePlayerListChanged(PlayerListChangedEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + event.getRoomId() + "/players",
            PlayerListMessage.from(event.getPlayers())
        );
    }
}
```

**장점**:
1. Application Layer가 UI 기술(WebSocket)에 의존하지 않음
2. 계층 분리 원칙 준수
3. UI 기술 변경(STOMP → SSE 등)이 Application Layer에 영향 없음

---

## 테스트 구조 개선

### 커밋
- `8261197`: 테스트 패키지 구조 정리 및 LogAspect 수정

### 변경 전: 테스트 코드가 분산되어 있음

```
test/
├── service/
│   ├── RoomServiceTest.java
│   └── PlayerServiceTest.java
├── domain/
│   └── RoomTest.java
└── controller/
    └── RoomControllerTest.java
```

**문제점**:
1. 테스트 픽스처가 중복됨
2. 테스트 베이스 클래스가 없음
3. 통합 테스트 설정이 반복됨

### 변경 후: 계층화된 테스트 구조

```
test/
├── global/                                  # 테스트 공통 코드
│   ├── ServiceTest.java                     # Service 테스트 베이스
│   ├── WebMvcIntegrationTest.java           # REST API 테스트 베이스
│   └── config/
│       ├── IntegrationTestConfig.java
│       ├── ServiceTestConfig.java
│       └── TestContainerConfig.java         # Testcontainers 설정
│
├── fixture/                                 # 테스트 픽스처
│   ├── TestDataHelper.java
│   ├── PlayerFixture.java
│   ├── RoomFixture.java
│   ├── RouletteFixture.java
│   └── WebSocketIntegrationTestSupport.java
│
└── [domain]/                                # 도메인별 테스트
    ├── application/
    │   └── RoomServiceTest.java
    ├── domain/
    │   ├── RoomTest.java
    │   └── PlayerTest.java
    ├── ui/
    │   └── RoomRestControllerTest.java
    └── infra/
        └── JpaRoomRepositoryTest.java
```

### 테스트 베이스 클래스

#### ServiceTest: Service 계층 테스트

```java
@ExtendWith(MockitoExtension.class)
public abstract class ServiceTest {
    // Mockito 설정
}
```

**사용 예시**:
```java
class RoomServiceTest extends ServiceTest {
    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomCommandService roomCommandService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void createRoom_shouldPublishEvent() {
        // Given
        String hostName = "host";
        Long menuId = 1L;

        // When
        roomService.createRoom(hostName, menuId);

        // Then
        verify(eventPublisher).publishEvent(any(RoomCreateEvent.class));
    }
}
```

#### WebMvcIntegrationTest: REST API 통합 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class WebMvcIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);
}
```

**사용 예시**:
```java
class RoomRestControllerTest extends WebMvcIntegrationTest {
    @Test
    void createRoom_shouldReturn200() throws Exception {
        // Given
        RoomCreateRequest request = new RoomCreateRequest("host", 1L);

        // When & Then
        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.joinCode").exists());
    }
}
```

#### WebSocketIntegrationTestSupport: WebSocket 통합 테스트

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class WebSocketIntegrationTestSupport {
    @LocalServerPort
    protected int port;

    protected WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    protected TestStompSession connect(String playerName) throws Exception {
        String url = "ws://localhost:" + port + "/ws";
        StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {})
            .get(3, TimeUnit.SECONDS);

        return new TestStompSession(session, playerName);
    }
}
```

**사용 예시**:
```java
class RoomWebSocketTest extends WebSocketIntegrationTestSupport {
    @Test
    void kickPlayer_shouldBroadcastPlayerList() throws Exception {
        // Given
        TestStompSession host = connect("host");
        TestStompSession guest = connect("guest");

        CompletableFuture<PlayerListMessage> future = new CompletableFuture<>();
        host.subscribe("/topic/room/ABC123/players", PlayerListMessage.class, future::complete);

        // When
        host.send("/app/room/ABC123/kick", new KickMessage("guest"));

        // Then
        PlayerListMessage message = future.get(3, TimeUnit.SECONDS);
        assertThat(message.getPlayers()).hasSize(1);
        assertThat(message.getPlayers().get(0).getName()).isEqualTo("host");
    }
}
```

### 테스트 픽스처

```java
public class RoomFixture {
    public static Room createRoom() {
        return Room.create(
            JoinCode.of("ABC123"),
            PlayerFixture.createHost()
        );
    }

    public static Room createRoomWithPlayers(int playerCount) {
        Room room = createRoom();
        for (int i = 1; i < playerCount; i++) {
            room.joinGuest(PlayerFixture.createGuest("guest" + i));
        }
        return room;
    }
}

public class PlayerFixture {
    public static Player createHost() {
        return Player.createHost(
            "host",
            MenuFixture.createMenu()
        );
    }

    public static Player createGuest(String name) {
        return Player.createGuest(
            name,
            MenuFixture.createMenu()
        );
    }
}
```

---

## 패키지 구조 변경

### 전체 패키지 구조

```
coffeeshout/
├── CoffeeShoutApplication.java
│
├── global/                          # 전역 설정 및 공통 코드
│   ├── config/                      # 설정 클래스
│   │   ├── AsyncConfig.java
│   │   ├── SwaggerConfig.java
│   │   ├── WebMvcConfig.java
│   │   ├── WebSocketMessageBrokerConfig.java
│   │   ├── redis/
│   │   │   ├── RedisConfig.java
│   │   │   ├── RedissonConfig.java
│   │   │   └── StreamConsumerConfig.java
│   │   └── aws/
│   │       └── AwsConfig.java
│   │
│   ├── exception/                   # 전역 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorCode.java
│   │   └── custom/
│   │       ├── InvalidArgumentException.java
│   │       └── InvalidStateException.java
│   │
│   ├── websocket/                   # WebSocket 공통 코드
│   │   ├── StompSessionManager.java
│   │   ├── PlayerDisconnectionService.java
│   │   └── event/
│   │       ├── SessionConnectEventListener.java
│   │       └── SessionDisconnectEventListener.java
│   │
│   ├── metric/                      # 메트릭 수집
│   │   ├── HttpMetricService.java
│   │   └── WebSocketMetricService.java
│   │
│   └── lock/                        # 분산 락
│       ├── RedisLock.java
│       └── RedisLockAspect.java
│
├── room/                            # Room 도메인
│   ├── ui/
│   │   ├── RoomRestController.java
│   │   ├── RoomWebSocketController.java
│   │   ├── messaging/
│   │   │   └── RoomMessagePublisher.java
│   │   ├── request/
│   │   └── response/
│   │
│   ├── application/
│   │   └── service/
│   │       ├── RoomService.java
│   │       ├── RoomPlayerService.java
│   │       ├── RoomMiniGameService.java
│   │       ├── RoomRouletteService.java
│   │       └── MenuService.java
│   │
│   ├── domain/
│   │   ├── Room.java
│   │   ├── Player.java
│   │   ├── Roulette.java
│   │   ├── JoinCode.java
│   │   ├── event/
│   │   │   ├── RoomCreateEvent.java
│   │   │   └── PlayerKickEvent.java
│   │   ├── menu/
│   │   │   ├── Menu.java
│   │   │   └── MenuCategory.java
│   │   ├── repository/
│   │   │   ├── RoomRepository.java
│   │   │   └── MenuRepository.java
│   │   └── service/
│   │       ├── RoomCommandService.java
│   │       ├── RoomQueryService.java
│   │       ├── PlayerCommandService.java
│   │       ├── MenuCommandService.java
│   │       └── MenuQueryService.java
│   │
│   └── infra/
│       ├── persistence/
│       │   ├── RoomEntity.java
│       │   ├── RoomJpaRepository.java
│       │   └── RoomPersistenceService.java
│       └── messaging/
│           ├── RedisRoomEventPublisher.java
│           ├── RoomEventSubscriber.java
│           └── handler/
│               ├── RoomCreateEventHandler.java
│               └── PlayerKickEventHandler.java
│
├── cardgame/                        # 카드게임 도메인
│   ├── ui/
│   ├── application/
│   ├── domain/
│   └── infra/
│
├── racinggame/                      # 레이싱게임 도메인
│   ├── ui/
│   ├── application/
│   ├── domain/
│   └── infra/
│
├── minigame/                        # 미니게임 공통
│   ├── ui/
│   ├── application/
│   ├── domain/
│   └── infra/
│
└── dashboard/                       # 대시보드
    ├── ui/
    ├── application/
    ├── domain/
    └── infra/
```

---

## 변경 이력

### 주요 리팩토링 커밋 타임라인

```
6143697 (베이스)
   ↓
[초기 구현]
d478b22 [feat] 룰렛을 돌려 당첨자를 반환한다.
76492c9 [feat] 카드게임 기능을 구현한다.
   ↓
[구조 개선 시작]
bb99797 refactor: RoomPlayerService, RoomMiniGameService, RoomRouletteService를 Domain Service로 이동
0925325 refactor: Domain Service 네이밍 개선 및 Application Layer 도입으로 계층 분리
   ↓
[이벤트 기반 전환]
d95beb8 fix: Event Handler들이 Domain Service를 사용하도록 수정
acd0d40 refactor: STOMP 브로드캐스트를 Spring Event 패턴으로 중앙화
   ↓
[계층 정리]
f54e59f refactor: MessagePublisher를 UI Layer로 이동 및 API 문서 중앙화
cb4c231 refactor: kickPlayer PlayerService에 위치하게 수정
7ab2f44 refactor: ApplicationService 네이밍을 Service로 통일
   ↓
[최종 정리]
d1d1bf0 refactor: application 패키지 정리
8261197 refactor: 테스트 패키지 구조 정리 및 LogAspect 수정
   ↓
HEAD (8261197)
```

---

## 리팩토링 체크리스트

### 새로운 기능 추가 시

- [ ] UI Layer: Controller 작성
- [ ] UI Layer: Request/Response DTO 작성
- [ ] Application Layer: Service 작성 (Use Case)
- [ ] Domain Layer: 도메인 모델 작성
- [ ] Domain Layer: Command/Query Service 작성
- [ ] Infrastructure Layer: Repository 구현
- [ ] 이벤트 정의 (필요 시)
- [ ] 이벤트 핸들러 작성 (필요 시)
- [ ] MessagePublisher 업데이트 (WebSocket 필요 시)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성

### 코드 리뷰 시 확인사항

- [ ] 계층 의존성 규칙 준수 (UI → Application → Domain ← Infrastructure)
- [ ] Service 네이밍 규칙 준수 (Service, CommandService, QueryService)
- [ ] 도메인 로직이 Domain Layer에 위치
- [ ] WebSocket 메시지 전송은 UI Layer의 MessagePublisher에서만
- [ ] 이벤트 기반 통신 사용 (직접 호출 최소화)
- [ ] 트랜잭션 경계는 Application Service
- [ ] 테스트 작성 완료

---

## 참고 자료

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
- [Spring Event Documentation](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

---

**작성일**: 2025-11-21
**마지막 업데이트**: 2025-11-21
