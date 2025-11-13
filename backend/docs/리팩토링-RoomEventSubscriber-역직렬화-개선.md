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

---

## ⚠️ Enum 방식의 잠재적 문제점 (사용자 피드백)

### 1. 컴파일 타임 의존성
```java
public enum RoomEventType {
    ROOM_CREATE(RoomCreateEvent.class),  // Enum이 모든 Event 클래스에 의존
    ROOM_JOIN(RoomJoinEvent.class),
    // ...
}
```

**문제:**
- Enum이 9개 Event 클래스에 대한 컴파일 타임 의존성을 가짐
- 하나의 Event 클래스만 수정해도 Enum이 재컴파일됨

**평가:**
- ⚠️ 실제로는 큰 문제 아님 - 같은 도메인 패키지 내에 있음
- ⚠️ 어차피 새 이벤트 추가 시 Enum은 수정되어야 함
- ✅ switch 문도 동일한 의존성을 가짐 (더 분산되어 있을 뿐)

### 2. "매번 추가해야 하는" 문제

**현실:**
- switch 문: 새 이벤트 추가 시 case 추가 필요
- Enum: 새 이벤트 추가 시 Enum 상수 추가 필요
- @JsonSubTypes: 새 이벤트 추가 시 @JsonSubTypes.Type 추가 필요

**결론:** 어떤 방식을 쓰든 새 이벤트는 어딘가에 선언해야 함

---

## 💡 옵션 4: 자동 스캔 방식 (완전 자동화)

"매번 추가하지 않아도 되는" 방식을 원한다면 자동 스캔이 유일한 해결책입니다.

### 구현 방식

```java
@Component
public class RoomEventTypeRegistry {

    private final Map<RoomEventType, Class<? extends RoomBaseEvent>> registry = new EnumMap<>(RoomEventType.class);
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void autoScanAndRegister() {
        // 1. RoomBaseEvent 구현체를 classpath에서 스캔
        ClassPathScanningCandidateComponentProvider scanner = 
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(RoomBaseEvent.class));

        Set<BeanDefinition> candidates = scanner.findCandidateComponents("coffeeshout.room.domain.event");

        for (BeanDefinition bd : candidates) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                if (RoomBaseEvent.class.isAssignableFrom(clazz)) {
                    // 2. 인스턴스를 만들어서 eventType() 호출
                    RoomBaseEvent instance = (RoomBaseEvent) clazz.getDeclaredConstructor().newInstance();
                    RoomEventType eventType = instance.eventType();
                    
                    // 3. 자동 등록
                    registry.put(eventType, (Class<? extends RoomBaseEvent>) clazz);
                    log.info("자동 등록: {} -> {}", eventType, clazz.getSimpleName());
                }
            } catch (Exception e) {
                log.error("이벤트 클래스 등록 실패: {}", bd.getBeanClassName(), e);
            }
        }
    }

    public Class<? extends RoomBaseEvent> getEventClass(RoomEventType eventType) {
        return registry.get(eventType);
    }
}

// RoomEventSubscriber.java
private RoomBaseEvent deserializeEvent(String body, RoomEventType eventType) throws Exception {
    return objectMapper.readValue(body, registry.getEventClass(eventType));
}
```

### 장점
- ✅ 새 이벤트 추가 시 클래스만 만들면 자동 등록
- ✅ 선언적 등록 불필요

### 단점
- ❌ **복잡도 대폭 증가**: 리플렉션, 클래스 스캔
- ❌ **런타임 오버헤드**: 애플리케이션 시작 시 스캔
- ❌ **디버깅 어려움**: 어떤 이벤트가 등록되는지 추적 어려움
- ❌ **명시성 상실**: 코드만 보고 어떤 이벤트가 있는지 알기 어려움
- ❌ **인스턴스 생성 문제**: 
  - RoomJoinEvent 같은 record는 기본 생성자가 없음
  - 더미 인스턴스를 만들어야 함 (부자연스러움)

### 대안: Annotation 기반

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RoomEventMapping {
    RoomEventType value();
}

@RoomEventMapping(RoomEventType.ROOM_CREATE)
public record RoomCreateEvent(...) implements RoomBaseEvent {
    // ...
}

// 스캔 시 어노테이션 읽기
RoomEventMapping annotation = clazz.getAnnotation(RoomEventMapping.class);
RoomEventType eventType = annotation.value();
registry.put(eventType, clazz);
```

**평가:**
- ✅ 인스턴스 생성 불필요
- ❌ 여전히 복잡하고 명시성이 떨어짐
- ❌ 어노테이션 자체가 "선언"임 (결국 매번 추가)

---

## 🎯 재평가: Enum 방식 vs 자동 스캔 vs 현상 유지

### 비교표

| 기준 | 현상 유지 (switch) | Enum 방식 | 자동 스캔 | Jackson subTypes |
|------|------------------|-----------|----------|------------------|
| **복잡도** | 단순 | 단순 | 복잡 | 중간 |
| **명시성** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐ | ⭐⭐⭐ |
| **타입 안정성** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| **새 이벤트 추가** | switch 수정 | Enum 수정 | 클래스만 생성 | 어노테이션 추가 |
| **컴파일 타임 체크** | ✅ | ✅ | ❌ | ✅ |
| **의존성 집중도** | 분산 (switch 내) | 집중 (Enum) | 집중 (Registry) | 집중 (어노테이션) |
| **디버깅** | 쉬움 | 쉬움 | 어려움 | 중간 |
| **성능** | 최고 | 최고 | 느림 (스캔) | 최고 |

### 핵심 질문: "매번 추가"가 실제로 문제인가?

**현실적 분석:**

1. **새 이벤트 추가 빈도**
   - 새 이벤트 타입은 자주 추가되지 않음
   - 도메인 이벤트는 비즈니스 요구사항과 직결
   - 추가될 때는 신중하게 검토 필요

2. **"매번 추가"의 실제 비용**
   - switch: `case NEW_EVENT -> objectMapper.readValue(body, NewEvent.class);` (1줄)
   - Enum: `NEW_EVENT(NewEvent.class),` (1줄)
   - 실제 비용 차이: **없음**

3. **자동 스캔의 실제 비용**
   - 복잡도 증가
   - 명시성 상실
   - 디버깅 어려움
   - 이득: 1줄 선언 생략 (미미함)

### 트레이드오프 평가

```
현상 유지 (switch 9줄) vs Enum 방식 (Enum 9줄)
→ 거의 동일한 비용, Enum이 약간 더 명시적

Enum 방식 vs 자동 스캔 (복잡한 리플렉션)
→ Enum 압도적 승리 (명시성, 단순성, 안정성)
```

---

## ✅ 최종 권장: Enum 방식

### 결론

**"매번 추가해야 하는" 것은 문제가 아닙니다.**

이유:
1. 새 이벤트는 어디선가 선언되어야 함 (피할 수 없음)
2. Enum에 명시하는 것이 가장 명확하고 타입 안전함
3. 자동 스캔의 복잡도는 1줄 선언 생략의 이득을 압도함
4. 컴파일 타임 의존성은 실제로 문제가 아님 (같은 도메인 내)

### Enum 방식의 실제 장점

```java
// Before: switch 문 (9줄)
return switch (eventType) {
    case ROOM_CREATE -> objectMapper.readValue(body, RoomCreateEvent.class);
    case ROOM_JOIN -> objectMapper.readValue(body, RoomJoinEvent.class);
    // ... 7줄 더
};

// After: Enum 활용 (1줄)
return objectMapper.readValue(body, eventType.getEventClass());
```

**획득:**
- ✅ 코드 8줄 감소
- ✅ 명시적 타입 매핑 (Enum에 집중)
- ✅ 컴파일 타임 안정성
- ✅ deserializeEvent 메서드 제거 가능

**비용:**
- Enum에 9개 상수 선언 (어차피 필요한 선언)

### 실제 적용 예시

```java
// RoomEventType.java (한 곳에 모든 매핑 명시)
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

// RoomEventSubscriber.java (극도로 단순화)
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
```

**새 이벤트 추가 시:**
```java
// 1단계: Event 클래스 생성
public record NewGameEvent(...) implements RoomBaseEvent {
    @Override
    public RoomEventType eventType() {
        return RoomEventType.NEW_GAME;
    }
}

// 2단계: Enum 추가 (딱 1줄)
public enum RoomEventType {
    // ... 기존 것들
    NEW_GAME(NewGameEvent.class);  // 이것만 추가!
}

// 끝! deserializeEvent switch 문 수정 불필요
```

---

## 📊 최종 결론

### Enum 방식을 강력 권장합니다

**이유:**
1. ✅ **간결함**: switch 9줄 → 1줄
2. ✅ **명시성**: Enum이 Single Source of Truth
3. ✅ **안정성**: 컴파일 타임 체크
4. ✅ **실용성**: "매번 추가"는 실제로 문제가 아님
5. ✅ **유지보수성**: 새 이벤트는 Enum만 수정

**"매번 추가해야 한다"는 것은:**
- 모든 방식이 동일함 (피할 수 없음)
- Enum에 명시하는 것이 가장 명확함
- 자동 스캔의 복잡도는 이득을 정당화하지 못함

진행하시겠습니까?
