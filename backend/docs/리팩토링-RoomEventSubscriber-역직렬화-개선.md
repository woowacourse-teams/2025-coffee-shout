# RoomEventSubscriber 리팩토링 분석 - eventType별 역직렬화 개선

## 📋 현재 문제점

### 1. 코드 구조
```java
private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) throws Exception {
    return switch (eventType) {
        case ROOM_CREATE -> objectMapper.readValue(body, RoomCreateEvent.class);
        case ROOM_JOIN -> objectMapper.readValue(body, RoomJoinEvent.class);
        case PLAYER_LIST_UPDATE -> objectMapper.readValue(body, PlayerListUpdateEvent.class);
        case PLAYER_READY -> objectMapper.readValue(body, PlayerReadyEvent.class);
        case PLAYER_KICK -> objectMapper.readValue(body, PlayerKickEvent.class);
        case MINI_GAME_SELECT -> objectMapper.readValue(body, MiniGameSelectEvent.class);
        case ROULETTE_SHOW -> objectMapper.readValue(body, RouletteShowEvent.class);
        case ROULETTE_SPIN -> objectMapper.readValue(body, RouletteSpinEvent.class);
        case QR_CODE_COMPLETE -> objectMapper.readValue(body, QrCodeStatusEvent.class);
    };
}
```

### 2. 문제점
- ❌ **OCP 위반**: 새 이벤트 타입 추가 시 switch 문 수정 필요
- ❌ **확장 불가능**: 이벤트 타입과 클래스가 강하게 결합
- ❌ **중복 패턴**: 모든 case가 동일한 패턴 (`objectMapper.readValue(body, XxxEvent.class)`)
- ❌ **유지보수 어려움**: 9개 case 문 관리

## 💡 개선 방안

### 옵션 1: Enum에 Class 정보 추가 (권장 ⭐)

**장점:**
- ✅ switch 문 완전 제거
- ✅ 타입 안정성 보장
- ✅ 새 이벤트 추가 시 Enum만 수정
- ✅ 컴파일 타임 체크

**구현:**

```java
// RoomEventType.java
public enum RoomEventType {
    ROOM_CREATE(RoomCreateEvent.class),
    ROOM_JOIN(RoomJoinEvent.class),
    PLAYER_READY(PlayerReadyEvent.class),
    PLAYER_LIST_UPDATE(PlayerListUpdateEvent.class),
    PLAYER_KICK(PlayerKickEvent.class),
    MINI_GAME_SELECT(MiniGameSelectEvent.class),
    ROULETTE_SHOW(RouletteShowEvent.class),
    ROULETTE_SPIN(RouletteSpinEvent.class),
    QR_CODE_COMPLETE(QrCodeStatusEvent.class);

    private final Class<? extends RoomBaseEvent> eventClass;

    RoomEventType(Class<? extends RoomBaseEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends RoomBaseEvent> getEventClass() {
        return eventClass;
    }
}

// RoomEventSubscriber.java
private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) throws Exception {
    return objectMapper.readValue(body, eventType.getEventClass());
}
```

**효과:**
- switch 문 9줄 → 1줄로 단순화
- 새 이벤트 추가 시 Enum만 수정 (OCP 준수)

---

### 옵션 2: Map 기반 Registry 패턴

**장점:**
- ✅ switch 문 제거
- ✅ 런타임 동적 등록 가능
- ✅ 설정 클래스로 분리 가능

**단점:**
- ❌ 컴파일 타임 체크 불가
- ❌ Map 관리 오버헤드
- ❌ 초기화 코드 필요

**구현:**

```java
@Component
public class RoomEventTypeRegistry {

    private final Map<RoomEventType, Class<? extends RoomBaseEvent>> registry = new EnumMap<>(RoomEventType.class);

    @PostConstruct
    public void init() {
        registry.put(RoomEventType.ROOM_CREATE, RoomCreateEvent.class);
        registry.put(RoomEventType.ROOM_JOIN, RoomJoinEvent.class);
        registry.put(RoomEventType.PLAYER_READY, PlayerReadyEvent.class);
        registry.put(RoomEventType.PLAYER_LIST_UPDATE, PlayerListUpdateEvent.class);
        registry.put(RoomEventType.PLAYER_KICK, PlayerKickEvent.class);
        registry.put(RoomEventType.MINI_GAME_SELECT, MiniGameSelectEvent.class);
        registry.put(RoomEventType.ROULETTE_SHOW, RouletteShowEvent.class);
        registry.put(RoomEventType.ROULETTE_SPIN, RouletteSpinEvent.class);
        registry.put(RoomEventType.QR_CODE_COMPLETE, QrCodeStatusEvent.class);
    }

    public Class<? extends RoomBaseEvent> getEventClass(RoomEventType eventType) {
        return registry.get(eventType);
    }
}

@Component
public class RoomEventSubscriber implements EventSubscriber {

    private final RoomEventTypeRegistry registry;

    private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) throws Exception {
        Class<? extends RoomBaseEvent> eventClass = registry.getEventClass(eventType);
        return objectMapper.readValue(body, eventClass);
    }
}
```

**평가:**
- Enum 방식보다 복잡하고 컴파일 타임 안정성이 떨어짐
- 런타임 동적 등록이 필요하지 않다면 불필요

---

### 옵션 3: Jackson Polymorphic Type Handling

**장점:**
- ✅ Jackson 내장 기능 활용
- ✅ 역직렬화 자동 처리
- ✅ eventType 추출 불필요

**단점:**
- ❌ RoomBaseEvent에 어노테이션 추가 필요
- ❌ JSON 구조에 의존
- ❌ 기존 구조 대폭 변경

**구현:**

```java
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RoomCreateEvent.class, name = "ROOM_CREATE"),
    @JsonSubTypes.Type(value = RoomJoinEvent.class, name = "ROOM_JOIN"),
    @JsonSubTypes.Type(value = PlayerReadyEvent.class, name = "PLAYER_READY"),
    @JsonSubTypes.Type(value = PlayerListUpdateEvent.class, name = "PLAYER_LIST_UPDATE"),
    @JsonSubTypes.Type(value = PlayerKickEvent.class, name = "PLAYER_KICK"),
    @JsonSubTypes.Type(value = MiniGameSelectEvent.class, name = "MINI_GAME_SELECT"),
    @JsonSubTypes.Type(value = RouletteShowEvent.class, name = "ROULETTE_SHOW"),
    @JsonSubTypes.Type(value = RouletteSpinEvent.class, name = "ROULETTE_SPIN"),
    @JsonSubTypes.Type(value = QrCodeStatusEvent.class, name = "QR_CODE_COMPLETE")
})
public interface RoomBaseEvent {
    RoomEventType eventType();
}

// RoomEventSubscriber.java
@Override
public void onMessage(Message message, byte[] pattern) {
    try {
        final String body = new String(message.getBody());
        // eventType 추출 불필요, Jackson이 자동 처리
        final RoomBaseEvent event = objectMapper.readValue(body, RoomBaseEvent.class);

        final RoomEventHandler<RoomBaseEvent> handler = handlerFactory.getHandler(event.eventType());
        handler.handle(event);
    } catch (Exception e) {
        log.error("이벤트 처리 실패", e);
    }
}
```

**평가:**
- Jackson 기능 활용으로 코드 간결화
- 하지만 도메인 클래스에 Jackson 어노테이션 추가 (인프라 의존)
- 기존 extractEventType 로직과 충돌

---

## 🎯 권장 사항

### **옵션 1 (Enum에 Class 정보 추가)을 강력히 권장합니다**

#### 이유:

1. **최소 변경**
   - Enum만 수정
   - 기존 로직 구조 유지
   - 도메인 클래스 무변경

2. **타입 안정성**
   - 컴파일 타임 체크
   - IDE 자동완성 지원
   - 리팩토링 안전

3. **명확성**
   - Enum이 eventType ↔ Class 매핑의 Single Source of Truth
   - 새 이벤트 추가 시 Enum 한 곳만 수정

4. **Producer 패턴과의 일관성**
   - Producer에서 RedisStreamPublisher 합성 사용
   - Enum에서 클래스 정보 관리 = 데이터 중심 접근
   - 두 가지 모두 간결하고 명확한 패턴

#### 구현 예시:

```java
// 1. Enum 수정 (domain layer)
public enum RoomEventType {
    ROOM_CREATE(RoomCreateEvent.class),
    ROOM_JOIN(RoomJoinEvent.class),
    PLAYER_READY(PlayerReadyEvent.class),
    PLAYER_LIST_UPDATE(PlayerListUpdateEvent.class),
    PLAYER_KICK(PlayerKickEvent.class),
    MINI_GAME_SELECT(MiniGameSelectEvent.class),
    ROULETTE_SHOW(RouletteShowEvent.class),
    ROULETTE_SPIN(RouletteSpinEvent.class),
    QR_CODE_COMPLETE(QrCodeStatusEvent.class);

    private final Class<? extends RoomBaseEvent> eventClass;

    RoomEventType(Class<? extends RoomBaseEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends RoomBaseEvent> getEventClass() {
        return eventClass;
    }
}

// 2. Subscriber 단순화 (infra layer)
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventSubscriber implements EventSubscriber {

    private final ObjectMapper objectMapper;
    private final RoomEventHandlerFactory handlerFactory;
    private final TracerProvider tracerProvider;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            final String body = new String(message.getBody());
            final RoomEventType eventType = extractEventType(body);

            if (!handlerFactory.canHandle(eventType)) {
                log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
                return;
            }

            // ✨ switch 문 제거! 1줄로 단순화
            final RoomBaseEvent event = objectMapper.readValue(body, eventType.getEventClass());

            final RoomEventHandler<RoomBaseEvent> handler = handlerFactory.getHandler(eventType);
            if (event instanceof Traceable traceable) {
                tracerProvider.executeWithTraceContext(
                        traceable.getTraceInfo(),
                        () -> handler.handle(event),
                        event.eventType().name()
                );
                return;
            }
            handler.handle(event);

        } catch (Exception e) {
            log.error("이벤트 처리 실패: message={}", new String(message.getBody()), e);
        }
    }

    private RoomEventType extractEventType(String body) throws Exception {
        final JsonNode jsonNode = objectMapper.readTree(body);
        final String eventTypeStr = jsonNode.get("eventType").asText();
        return RoomEventType.valueOf(eventTypeStr);
    }

    // deserializeEvent 메서드 삭제 - 필요 없음!
}
```

#### 효과:
- **9줄 switch 문 삭제**
- **deserializeEvent 메서드 삭제**
- **onMessage 메서드 직접 역직렬화로 간소화**
- **새 이벤트 추가 시 Enum 1줄만 추가**

---

## 📊 비교표

| 기준 | 옵션 1 (Enum) | 옵션 2 (Map Registry) | 옵션 3 (Jackson) |
|------|--------------|----------------------|------------------|
| **코드 간결성** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **타입 안정성** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **변경 범위** | Enum만 | Config + Enum | Domain 클래스 |
| **컴파일 타임 체크** | ✅ | ❌ | ✅ |
| **새 이벤트 추가** | Enum 1줄 | Map 1줄 + Enum | 어노테이션 1줄 |
| **도메인 순수성** | ✅ | ✅ | ❌ (Jackson 의존) |
| **기존 구조 유지** | ✅ | ⭐⭐⭐ | ❌ |

## 결론

**옵션 1 (Enum에 Class 정보 추가)**를 구현하면:
- switch 문 9줄 제거
- deserializeEvent 메서드 제거
- OCP 준수
- 새 이벤트 추가 시 Enum만 수정

진행하시겠습니까?
