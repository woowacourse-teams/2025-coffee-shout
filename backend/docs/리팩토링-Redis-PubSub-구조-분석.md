# Redis Pub/Sub 구조 분석 및 개선 방안

## 📋 현재 아키텍처

### 1. 구조 다이어그램

```
┌─────────────────────────────────────────────────────┐
│  EventTopicRegistry (Enum)                          │
│  - ROOM("room.events")                              │
│  - MINI_GAME("minigame.events")                     │
│  - PLAYER("player.events")                          │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  SubscriberAutoConfiguration                        │
│  - 모든 EventSubscriber를 자동 스캔하여 등록       │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  RoomEventSubscriber implements EventSubscriber     │
│  1. Redis Pub/Sub 메시지 수신                      │
│  2. eventType 추출 (extractEventType)              │
│  3. 역직렬화 (deserializeEvent) ← switch 9개       │
│  4. Handler 가져오기 (handlerFactory)              │
│  5. Handler 실행                                    │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  RoomEventHandlerFactory                            │
│  - Handler 자동 스캔 및 Map 관리 ✅ 이미 좋음!     │
│  - getSupportedEventType()으로 자동 매핑           │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  RoomEventHandler 구현체들                          │
│  - RoomCreateEventHandler                           │
│  - RoomJoinEventHandler                             │
│  - PlayerReadyEventHandler                          │
│  - ... (9개)                                        │
└─────────────────────────────────────────────────────┘
```

### 2. 주요 컴포넌트

#### EventTopicRegistry
```java
public enum EventTopicRegistry {
    ROOM("room.events"),           // 9개 이벤트 타입
    MINI_GAME("minigame.events"),
    PLAYER("player.events"),
    SESSION("session.events");
}
```

#### SubscriberAutoConfiguration
```java
@PostConstruct
public void registerAllSubscribers() {
    subscribers.forEach(subscriber -> {
        container.addMessageListener(subscriber, topic);
    });
}
```
✅ **이미 좋음**: 자동 스캔 및 등록

#### RoomEventHandlerFactory
```java
public RoomEventHandlerFactory(List<RoomEventHandler<? extends RoomBaseEvent>> handlers) {
    this.handlerMap = handlers.stream()
            .collect(Collectors.toMap(
                    RoomEventHandler::getSupportedEventType,
                    Function.identity()
            ));
}
```
✅ **이미 좋음**: Spring DI로 자동 등록, Map으로 관리

#### RoomEventSubscriber
```java
@Override
public void onMessage(Message message, byte[] pattern) {
    final RoomEventType eventType = extractEventType(body);
    final RoomBaseEvent event = deserializeEvent(body, eventType);  // ← switch 9개
    final RoomEventHandler<RoomBaseEvent> handler = handlerFactory.getHandler(eventType);
    handler.handle(event);
}
```
⚠️ **개선 필요**: deserializeEvent의 switch 문

---

## 🔍 Stream vs Pub/Sub 비교

### GenericStreamConsumer가 가능한 이유

```
Stream: 1 stream key = 1 event type
┌──────────────────────────────────┐
│ roomJoinKey                      │
│  → RoomJoinEvent만 발행           │
│  → GenericStreamConsumer<RoomJoinEvent> │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ cardSelectKey                    │
│  → SelectCardCommandEvent만 발행  │
│  → GenericStreamConsumer<SelectCardCommandEvent> │
└──────────────────────────────────┘
```

**특징:**
- 하나의 Stream에 하나의 이벤트 타입만 흐름
- 역직렬화 타입이 명확함 (Class<T>)
- 제너릭 Consumer로 통합 가능 ✅

### Pub/Sub는 다름

```
Topic: 1 topic = N event types
┌──────────────────────────────────┐
│ room.events                      │
│  → RoomCreateEvent               │
│  → RoomJoinEvent                 │
│  → PlayerReadyEvent              │
│  → PlayerListUpdateEvent         │
│  → ... (9개 이벤트 타입)         │
└──────────────────────────────────┘
```

**특징:**
- 하나의 Topic에 여러 이벤트 타입이 섞여서 옴
- 메시지를 받아야 eventType을 알 수 있음
- 제너릭 Subscriber로 통합 불가능 ❌

---

## 💡 Pub/Sub 개선 방안

### 옵션 1: Enum에 Class 추가 (권장 ⭐)

**현재 문제:**
```java
private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) {
    return switch (eventType) {
        case ROOM_CREATE -> objectMapper.readValue(body, RoomCreateEvent.class);
        case ROOM_JOIN -> objectMapper.readValue(body, RoomJoinEvent.class);
        // ... 9개
    };
}
```

**해결:**
```java
// RoomEventType.java
public enum RoomEventType {
    ROOM_CREATE(RoomCreateEvent.class),
    ROOM_JOIN(RoomJoinEvent.class),
    // ...

    private final Class<? extends RoomBaseEvent> eventClass;

    RoomEventType(Class<? extends RoomBaseEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends RoomBaseEvent> getEventClass() {
        return eventClass;
    }
}

// RoomEventSubscriber.java
final RoomBaseEvent event = objectMapper.readValue(body, eventType.getEventClass());
// switch 문 완전 제거!
```

**효과:**
- ✅ switch 9줄 → 1줄
- ✅ deserializeEvent 메서드 제거 가능
- ✅ 컴파일 타임 안정성

---

### 옵션 2: Topic 분리 (비권장)

**아이디어:** 이벤트 타입별로 Topic을 분리

```java
public enum EventTopicRegistry {
    ROOM_CREATE("room.create"),
    ROOM_JOIN("room.join"),
    PLAYER_READY("player.ready"),
    // ...
}
```

**각 이벤트 타입당 Subscriber 생성:**
```java
@Component
public class RoomCreateSubscriber implements EventSubscriber {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // eventType 추출 불필요
        RoomCreateEvent event = objectMapper.readValue(body, RoomCreateEvent.class);
        handler.handle(event);
    }
}

@Component
public class RoomJoinSubscriber implements EventSubscriber {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        RoomJoinEvent event = objectMapper.readValue(body, RoomJoinEvent.class);
        handler.handle(event);
    }
}

// 9개 Subscriber...
```

**평가:**
- ❌ **큰 구조 변경**: Topic 9개 → Subscriber 9개
- ❌ **Redis 연결 증가**: Topic마다 별도 구독
- ❌ **보일러플레이트 증가**: Subscriber 클래스 9개
- ⚠️ **이득 미미**: switch 문 제거하는 데 너무 큰 비용

**결론:** Enum 방식으로 충분함

---

### 옵션 3: GenericSubscriber 시도 (불가능)

**시도해볼 수 있지만 실패:**

```java
// 이론적으로...
public class GenericSubscriber<T extends RoomBaseEvent> implements EventSubscriber {
    private final Class<T> eventClass;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        T event = objectMapper.readValue(body, eventClass);
        // ...
    }
}
```

**문제:**
- ❌ 하나의 Topic에 여러 이벤트 타입이 옴
- ❌ eventClass를 미리 알 수 없음
- ❌ 메시지 받아서 eventType 추출 후에야 알 수 있음

**결론:** Pub/Sub 특성상 불가능

---

## 🎯 현재 아키텍처 평가

### 잘 설계된 부분 ✅

1. **SubscriberAutoConfiguration**
   - 모든 EventSubscriber 자동 스캔 및 등록
   - 새 Subscriber 추가 시 자동 인식

2. **RoomEventHandlerFactory**
   - Handler 자동 스캔 및 Map 관리
   - getSupportedEventType()으로 매핑
   - 새 Handler 추가 시 자동 인식

3. **EventTopicRegistry**
   - Topic 이름 중앙 관리
   - Enum으로 타입 안정성

### 개선 필요한 부분 ⚠️

1. **RoomEventSubscriber.deserializeEvent()**
   - switch 문 9개
   - Enum에 Class 추가로 해결 가능

---

## ✅ 최종 권장 사항

### Enum 방식만 적용하면 충분합니다

**이유:**
1. **최소 변경**: Enum만 수정, 기존 구조 유지
2. **최대 효과**: switch 9줄 → 1줄
3. **다른 부분은 이미 좋음**:
   - SubscriberAutoConfiguration ✅
   - RoomEventHandlerFactory ✅
   - Topic 구조 ✅

### 구현

```java
// 1. RoomEventType 수정
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

// 2. RoomEventSubscriber 단순화
@Override
public void onMessage(Message message, byte[] pattern) {
    try {
        final String body = new String(message.getBody());
        final RoomEventType eventType = extractEventType(body);

        if (!handlerFactory.canHandle(eventType)) {
            log.warn("처리할 수 없는 이벤트 타입: {}", eventType);
            return;
        }

        // ✨ switch 문 제거, 1줄로 단순화
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
        log.error("이벤트 처리 실패", e);
    }
}

// deserializeEvent 메서드 삭제!
```

### 효과

**Before:**
- RoomEventType: 단순 Enum (9개 상수)
- RoomEventSubscriber: extractEventType (5줄) + deserializeEvent (12줄)

**After:**
- RoomEventType: Class 정보 포함 Enum (18줄)
- RoomEventSubscriber: extractEventType (5줄) + 직접 역직렬화 (1줄)

**순수익:** 12줄 감소 + 명시성 증가

---

## 📊 Stream vs Pub/Sub 정리

| 특성 | Stream | Pub/Sub |
|------|--------|---------|
| **구조** | 1 stream = 1 event type | 1 topic = N event types |
| **Consumer 통합** | ✅ 가능 (GenericStreamConsumer) | ❌ 불가능 |
| **역직렬화** | 타입 명확 (제너릭) | eventType 추출 필요 |
| **개선 방법** | 공통 Consumer | Enum에 Class 추가 |

## 결론

**Redis Pub/Sub는 현재 구조가 이미 잘 설계되어 있습니다.**
- ✅ SubscriberAutoConfiguration (자동 등록)
- ✅ RoomEventHandlerFactory (자동 매핑)
- ⚠️ RoomEventSubscriber (switch 문) → Enum으로 개선

**Enum에 Class 정보만 추가하면 충분합니다.**

진행하시겠습니까?
