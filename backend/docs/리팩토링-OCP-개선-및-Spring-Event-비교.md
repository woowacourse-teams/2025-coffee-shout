# OCP 개선 및 Spring Event 비교 분석

## 📋 문제 정의

### OCP(Open-Closed Principle) 위반
```java
// ❌ 새 이벤트 타입 추가 시 이 클래스를 수정해야 함
public class RedisEventPublisher implements EventPublisher {
    public void publish(RoomBaseEvent event) {
        if (event instanceof RoomJoinEvent) {
            publishToStream(event);
        } else {
            publishToPubSub(event);
        }
    }
}
```

**문제점:**
- 확장(새 이벤트 추가)을 위해 기존 코드 수정 필요
- 이벤트 타입이 늘어날수록 if-else 증가
- 단위 테스트 시 모든 분기를 테스트해야 함

## 🎯 개선 방안 비교

### 방안 1: 전략 패턴 (Strategy Pattern)

#### 설계
```java
/**
 * 발행 전략 인터페이스
 */
public interface PublishStrategy {
    /**
     * 이 전략이 해당 이벤트를 처리할 수 있는지 확인
     */
    boolean supports(RoomBaseEvent event);

    /**
     * 이벤트를 발행한다
     */
    void publish(RoomBaseEvent event);

    /**
     * 전략의 우선순위 (낮을수록 우선)
     */
    default int getOrder() {
        return 100;
    }
}

/**
 * Redis Pub/Sub 전략
 */
@Component
@Order(200)  // 기본 전략이므로 낮은 우선순위
public class PubSubPublishStrategy implements PublishStrategy {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TopicManager topicManager;

    @Override
    public boolean supports(RoomBaseEvent event) {
        // 다른 전략이 처리하지 않는 모든 이벤트를 처리
        return true;
    }

    @Override
    public void publish(RoomBaseEvent event) {
        String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
        redisTemplate.convertAndSend(topic, event);
        log.debug("Redis Pub/Sub로 발행: {}", event.eventType());
    }
}

/**
 * Redis Stream 전략 (순서 보장이 필요한 이벤트)
 */
@Component
@Order(100)  // 높은 우선순위
public class StreamPublishStrategy implements PublishStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(RoomBaseEvent event) {
        // RoomJoinEvent만 처리
        return event instanceof RoomJoinEvent;
    }

    @Override
    public void publish(RoomBaseEvent event) {
        String eventJson = objectMapper.writeValueAsString(event);
        Record<String, String> record = StreamRecords.newRecord()
            .in(properties.roomJoinKey())
            .ofObject(eventJson);

        stringRedisTemplate.opsForStream().add(record);
        log.debug("Redis Stream으로 발행: {}", event.eventType());
    }
}

/**
 * 전략을 사용하는 Publisher
 */
@Component
public class RedisEventPublisher implements EventPublisher {

    private final List<PublishStrategy> strategies;

    public RedisEventPublisher(List<PublishStrategy> strategies) {
        // Order 애노테이션에 따라 자동 정렬됨
        this.strategies = strategies;
    }

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        PublishStrategy strategy = strategies.stream()
            .filter(s -> s.supports(event))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "지원하는 전략을 찾을 수 없습니다: " + event.getClass()));

        strategy.publish(event);
        log.info("이벤트 발행 완료: eventType={}", event.eventType());
    }
}
```

#### 새 전략 추가 예시 (기존 코드 수정 없음!)
```java
/**
 * Kafka 발행 전략 추가
 */
@Component
@Order(50)  // 가장 높은 우선순위
public class KafkaPublishStrategy implements PublishStrategy {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public boolean supports(RoomBaseEvent event) {
        return event instanceof PaymentEvent;  // 결제 이벤트만
    }

    @Override
    public void publish(RoomBaseEvent event) {
        kafkaTemplate.send("payment-topic", event);
        log.debug("Kafka로 발행: {}", event.eventType());
    }
}

// ✅ RedisEventPublisher 코드는 전혀 수정하지 않음!
```

#### 장단점
**장점:**
- ✅ OCP 준수: 새 전략 추가 시 기존 코드 수정 불필요
- ✅ 단일 책임 원칙: 각 전략이 하나의 발행 메커니즘만 담당
- ✅ 테스트 용이: 각 전략을 독립적으로 테스트 가능
- ✅ 우선순위 제어: @Order로 전략 적용 순서 관리

**단점:**
- ❌ 클래스 수 증가 (전략마다 클래스 필요)
- ❌ 약간의 런타임 오버헤드 (리스트 순회)
- ❌ 전략 간 우선순위 관리 필요

---

### 방안 2: 이벤트 메타데이터 방식

#### 설계
```java
/**
 * 발행 전략 타입
 */
public enum PublishingStrategy {
    BROADCAST,      // Redis Pub/Sub
    ORDERED,        // Redis Stream (순서 보장)
    PERSISTENT,     // DB 저장 후 배치 처리
    KAFKA           // Kafka
}

/**
 * 이벤트 인터페이스에 메타데이터 추가
 */
public interface RoomBaseEvent {
    String eventId();
    Instant timestamp();
    RoomEventType eventType();

    /**
     * 이 이벤트의 발행 전략을 반환
     * - 기본값: BROADCAST (Pub/Sub)
     */
    default PublishingStrategy getPublishingStrategy() {
        return PublishingStrategy.BROADCAST;
    }
}

/**
 * 각 이벤트가 자신의 발행 전략을 선언
 */
public record RoomCreateEvent(
    String eventId,
    Instant timestamp,
    String hostName,
    String joinCode
) implements RoomBaseEvent {

    @Override
    public RoomEventType eventType() {
        return RoomEventType.ROOM_CREATE;
    }

    // 기본 전략 사용 (BROADCAST)
}

public record RoomJoinEvent(
    String eventId,
    Instant timestamp,
    String joinCode,
    String guestName
) implements RoomBaseEvent {

    @Override
    public RoomEventType eventType() {
        return RoomEventType.ROOM_JOIN;
    }

    @Override
    public PublishingStrategy getPublishingStrategy() {
        return PublishingStrategy.ORDERED;  // ⭐ 순서 보장 필요
    }
}

/**
 * Publisher 구현
 */
@Component
public class RedisEventPublisher implements EventPublisher {

    private final Map<PublishingStrategy, PublishHandler> handlers;

    public RedisEventPublisher(
        RedisTemplate<String, Object> redisTemplate,
        StringRedisTemplate stringRedisTemplate,
        TopicManager topicManager,
        RedisStreamProperties properties,
        ObjectMapper objectMapper
    ) {
        this.handlers = Map.of(
            PublishingStrategy.BROADCAST,
                new PubSubHandler(redisTemplate, topicManager),
            PublishingStrategy.ORDERED,
                new StreamHandler(stringRedisTemplate, properties, objectMapper)
        );
    }

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        PublishingStrategy strategy = event.getPublishingStrategy();

        PublishHandler handler = handlers.get(strategy);
        if (handler == null) {
            throw new IllegalStateException("지원하지 않는 전략: " + strategy);
        }

        handler.handle(event);
        log.info("이벤트 발행 완료: eventType={}, strategy={}",
            event.eventType(), strategy);
    }

    // 내부 핸들러 인터페이스
    private interface PublishHandler {
        void handle(RoomBaseEvent event);
    }

    private record PubSubHandler(
        RedisTemplate<String, Object> redisTemplate,
        TopicManager topicManager
    ) implements PublishHandler {
        @Override
        public void handle(RoomBaseEvent event) {
            String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
            redisTemplate.convertAndSend(topic, event);
        }
    }

    private record StreamHandler(
        StringRedisTemplate stringRedisTemplate,
        RedisStreamProperties properties,
        ObjectMapper objectMapper
    ) implements PublishHandler {
        @Override
        public void handle(RoomBaseEvent event) {
            String eventJson = objectMapper.writeValueAsString(event);
            Record<String, String> record = StreamRecords.newRecord()
                .in(properties.roomJoinKey())
                .ofObject(eventJson);
            stringRedisTemplate.opsForStream().add(record);
        }
    }
}
```

#### 새 전략 추가 예시
```java
// 1. Enum에 추가
public enum PublishingStrategy {
    BROADCAST,
    ORDERED,
    KAFKA  // ⭐ 추가
}

// 2. Handler Map에 추가
this.handlers = Map.of(
    PublishingStrategy.BROADCAST, new PubSubHandler(...),
    PublishingStrategy.ORDERED, new StreamHandler(...),
    PublishingStrategy.KAFKA, new KafkaHandler(...)  // ⭐ 추가
);

// 3. 이벤트에서 사용
public record PaymentEvent(...) implements RoomBaseEvent {
    @Override
    public PublishingStrategy getPublishingStrategy() {
        return PublishingStrategy.KAFKA;  // ⭐ 사용
    }
}
```

#### 장단점
**장점:**
- ✅ instanceof 체크 제거
- ✅ 이벤트가 자신의 발행 방식을 선언 (자기 문서화)
- ✅ Map 기반 디스패칭으로 빠름
- ✅ 클래스 수 적음

**단점:**
- ❌ 도메인 이벤트에 인프라 관련 메타데이터 추가 (계층 오염)
- ❌ 새 전략 추가 시 여전히 Publisher 코드 수정 필요 (Enum + Map)
- ❌ 이벤트마다 전략을 명시해야 함 (기본값 있지만)

---

### 방안 3: 애노테이션 기반

#### 설계
```java
/**
 * 발행 설정 애노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublishingConfig {
    PublishingStrategy strategy() default PublishingStrategy.BROADCAST;
}

/**
 * 애노테이션을 사용한 이벤트 정의
 */
@PublishingConfig(strategy = PublishingStrategy.BROADCAST)
public record RoomCreateEvent(...) implements RoomBaseEvent {
    // 구현
}

@PublishingConfig(strategy = PublishingStrategy.ORDERED)
public record RoomJoinEvent(...) implements RoomBaseEvent {
    // 구현
}

/**
 * Publisher 구현
 */
@Component
public class RedisEventPublisher implements EventPublisher {

    private final Map<PublishingStrategy, PublishHandler> handlers;

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        PublishingStrategy strategy = extractStrategy(event);

        PublishHandler handler = handlers.get(strategy);
        handler.handle(event);
    }

    private PublishingStrategy extractStrategy(RoomBaseEvent event) {
        PublishingConfig config = event.getClass()
            .getAnnotation(PublishingConfig.class);

        return config != null
            ? config.strategy()
            : PublishingStrategy.BROADCAST;  // 기본값
    }
}
```

#### 장단점
**장점:**
- ✅ 이벤트 정의부에서 전략이 명확히 보임
- ✅ 메서드 오버라이드 불필요 (메타데이터 방식보다 간결)

**단점:**
- ❌ 리플렉션 사용 (약간의 성능 오버헤드)
- ❌ 런타임에만 오류 발견 가능
- ❌ 여전히 Publisher 코드 수정 필요 (새 전략 추가 시)

---

## 🎯 방안 추천: 전략 패턴 (Strategy Pattern)

### 추천 이유

1. **진정한 OCP 준수**
   - 새 전략 추가 시 기존 코드 수정 불필요
   - 단순히 새 `@Component` 클래스만 추가

2. **테스트 용이성**
   ```java
   @Test
   void pubSubStrategyTest() {
       // ✅ 전략 하나만 테스트
       PubSubPublishStrategy strategy = new PubSubPublishStrategy(...);
       strategy.publish(new RoomCreateEvent(...));
   }
   ```

3. **명확한 책임 분리**
   - 각 전략이 독립적인 클래스
   - 복잡한 로직은 전략 내부에 캡슐화

4. **Spring 친화적**
   - `@Component` + `@Order`로 자동 관리
   - 의존성 주입 활용

### 단점 보완

**클래스 수 증가 문제:**
- 현재는 2-3개 전략만 필요 → 문제 없음
- 10개 이상이 되면 그때 리팩토링 고려

**성능 문제:**
- 리스트 순회 오버헤드는 미미 (나노초 단위)
- 이벤트 발행은 I/O 작업이므로 순회 비용 무시 가능

---

## 2️⃣ Spring Event vs 커스텀 EventPublisher 비교

### 옵션 A: Spring ApplicationEventPublisher

#### 설계
```java
/**
 * Application Layer
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final ApplicationEventPublisher eventPublisher;  // ⭐ Spring 제공

    @Transactional
    public Room createRoom(String hostName, SelectedMenuRequest request) {
        // 비즈니스 로직...

        RoomCreateEvent event = new RoomCreateEvent(...);
        eventPublisher.publishEvent(event);  // ⭐ Spring Event 발행

        return room;
    }
}

/**
 * Infra Layer - Event Listener
 */
@Component
@RequiredArgsConstructor
public class RedisEventListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final TopicManager topicManager;

    /**
     * RoomCreateEvent 처리 - Pub/Sub
     */
    @EventListener
    public void handleRoomCreate(RoomCreateEvent event) {
        String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
        redisTemplate.convertAndSend(topic, event);
        log.info("Pub/Sub로 발행: {}", event.eventType());
    }

    /**
     * RoomJoinEvent 처리 - Stream
     */
    @EventListener
    @Async  // ⭐ 비동기 처리 가능
    public void handleRoomJoin(RoomJoinEvent event) {
        String eventJson = objectMapper.writeValueAsString(event);
        Record<String, String> record = StreamRecords.newRecord()
            .in(properties.roomJoinKey())
            .ofObject(eventJson);
        stringRedisTemplate.opsForStream().add(record);
        log.info("Stream으로 발행: {}", event.eventType());
    }

    /**
     * 트랜잭션 커밋 후 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomCreateAfterCommit(RoomCreateEvent event) {
        // DB 커밋 후에만 Redis 발행
        log.info("트랜잭션 커밋 후 처리: {}", event.eventType());
    }
}
```

#### 장점
✅ **Spring 생태계 활용**
- `@TransactionalEventListener`: 트랜잭션과 통합
- `@Async`: 비동기 처리 쉽게 적용
- `@Order`: 리스너 실행 순서 제어
- 테스트 지원: `@EventListener` Mock 가능

✅ **간단한 설정**
```java
// 추가 인터페이스 정의 불필요
// ApplicationEventPublisher는 Spring이 제공
```

✅ **로컬 이벤트 + 원격 이벤트 분리**
```java
@EventListener  // 로컬 캐시 업데이트
public void handleLocal(RoomCreateEvent event) {
    cache.update(event);
}

@EventListener  // Redis로 원격 발행
public void handleRemote(RoomCreateEvent event) {
    redisTemplate.convertAndSend(topic, event);
}
```

✅ **유연한 이벤트 처리**
```java
// 여러 리스너가 동일 이벤트 처리 가능
@EventListener
public void handleForMetrics(RoomCreateEvent event) {
    metrics.record(event);
}

@EventListener
public void handleForAudit(RoomCreateEvent event) {
    auditLog.save(event);
}

@EventListener
public void handleForRedis(RoomCreateEvent event) {
    redisTemplate.convertAndSend(topic, event);
}
```

#### 단점
❌ **의존성 방향 문제**
```java
// Application Layer
@Service
public class RoomService {
    private final ApplicationEventPublisher eventPublisher;  // ⚠️ Spring 프레임워크 의존
}
```
- 도메인/애플리케이션이 Spring에 강하게 결합
- 프레임워크 교체 시 어려움 (하지만 현실적으로 드묾)

❌ **암시적 동작**
```java
eventPublisher.publishEvent(event);
// 어떤 리스너가 실행되는지 코드만 보고 알기 어려움
// IDE 지원으로 어느 정도 해결 가능
```

❌ **타입 안전성 부족**
```java
// 리스너가 없어도 컴파일 오류 없음
eventPublisher.publishEvent(new SomeEvent());  // 리스너 없으면 조용히 무시됨
```

---

### 옵션 B: 커스텀 EventPublisher (현재 제안)

#### 설계
```java
/**
 * Domain Layer - 인터페이스 정의
 */
public interface EventPublisher {
    <T extends RoomBaseEvent> void publish(T event);
}

/**
 * Application Layer
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final EventPublisher eventPublisher;  // ⭐ 커스텀 인터페이스

    @Transactional
    public Room createRoom(...) {
        RoomCreateEvent event = new RoomCreateEvent(...);
        eventPublisher.publish(event);  // ⭐ 명시적 발행
        return room;
    }
}

/**
 * Infra Layer - 구현체
 */
@Component
public class RedisEventPublisher implements EventPublisher {

    private final List<PublishStrategy> strategies;

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        PublishStrategy strategy = strategies.stream()
            .filter(s -> s.supports(event))
            .findFirst()
            .orElseThrow();
        strategy.publish(event);
    }
}
```

#### 장점
✅ **명시적 의존성**
```java
// Application이 자신만의 인터페이스에 의존
private final EventPublisher eventPublisher;  // 도메인/애플리케이션 계층 인터페이스
```

✅ **타입 안전성**
```java
public interface EventPublisher {
    <T extends RoomBaseEvent> void publish(T event);  // ⭐ RoomBaseEvent만 허용
}

// ❌ 컴파일 에러
eventPublisher.publish(new String("invalid"));
```

✅ **명확한 제어 흐름**
```java
eventPublisher.publish(event);
// → RedisEventPublisher.publish() 호출
// → StreamPublishStrategy 또는 PubSubPublishStrategy 실행
// 추적 가능
```

✅ **테스트 용이성**
```java
@Test
void createRoomTest() {
    EventPublisher mockPublisher = mock(EventPublisher.class);
    RoomService service = new RoomService(mockPublisher, ...);

    service.createRoom(...);

    verify(mockPublisher).publish(any(RoomCreateEvent.class));  // ⭐ 명확한 검증
}
```

#### 단점
❌ **보일러플레이트 코드**
```java
// 인터페이스 + 구현체 직접 작성
public interface EventPublisher { ... }
public class RedisEventPublisher implements EventPublisher { ... }
```

❌ **Spring 기능 활용 제한**
```java
// @TransactionalEventListener 사용 불가
// @Async 직접 구현 필요
```

❌ **이벤트 멀티캐스팅 어려움**
```java
// 하나의 이벤트를 여러 곳에서 처리하려면 복잡해짐
// Spring Event는 자동으로 모든 리스너 호출
```

---

## 🎯 상황별 추천

### Case 1: 로컬 이벤트 + 원격 이벤트 둘 다 필요

**추천: Spring Event**

```java
@EventListener
public void updateLocalCache(RoomCreateEvent event) {
    cache.update(event);  // 로컬
}

@EventListener
public void publishToRedis(RoomCreateEvent event) {
    redisTemplate.convertAndSend(topic, event);  // 원격
}

@EventListener
public void recordMetrics(RoomCreateEvent event) {
    metrics.increment("room.created");  // 로컬
}
```

### Case 2: 원격 발행만 필요 + 명시적 제어 중요

**추천: 커스텀 EventPublisher**

```java
// 명확하고 단순
eventPublisher.publish(new RoomCreateEvent(...));
```

### Case 3: 트랜잭션과 긴밀한 통합 필요

**추천: Spring Event**

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleAfterCommit(RoomCreateEvent event) {
    // DB 커밋 후에만 실행 보장
}
```

---

## 💡 하이브리드 접근: 두 가지 병행

### 설계
```java
/**
 * Application Layer
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final EventPublisher eventPublisher;              // 원격 발행
    private final ApplicationEventPublisher springPublisher;  // 로컬 이벤트

    @Transactional
    public Room createRoom(...) {
        RoomCreateEvent event = new RoomCreateEvent(...);

        // 1. 로컬 이벤트 발행 (캐시, 메트릭 등)
        springPublisher.publishEvent(event);

        // 2. 원격 발행 (Redis)
        eventPublisher.publish(event);

        return room;
    }
}

/**
 * 로컬 이벤트 처리
 */
@Component
public class RoomLocalEventListener {

    @EventListener
    public void updateCache(RoomCreateEvent event) {
        cache.update(event);
    }

    @EventListener
    public void recordMetrics(RoomCreateEvent event) {
        metrics.increment("room.created");
    }
}
```

#### 장점
- ✅ 로컬/원격 이벤트 명확히 분리
- ✅ 각 도구의 장점 활용

#### 단점
- ❌ 복잡성 증가
- ❌ 이벤트가 두 번 발행됨 (혼란 가능)

---

## 📊 최종 추천

### 현재 프로젝트에 가장 적합한 조합

```
커스텀 EventPublisher (원격 발행)
    +
전략 패턴 (OCP 준수)
```

#### 이유:

1. **명확성**
   - Application이 원격 이벤트 발행을 명시적으로 제어
   - 로컬 이벤트는 현재 `RoomEventLocalHandler`로 충분

2. **OCP 준수**
   - 전략 패턴으로 새 발행 메커니즘 추가 시 기존 코드 수정 불필요

3. **테스트 용이성**
   - Mock 하나로 모든 이벤트 발행 검증 가능

4. **점진적 도입**
   - 필요 시 나중에 Spring Event 추가 가능

### 구현 우선순위

1. **Phase 1**: 커스텀 `EventPublisher` 인터페이스 생성
2. **Phase 2**: 전략 패턴 기반 `RedisEventPublisher` 구현
   - `PubSubPublishStrategy`
   - `StreamPublishStrategy`
3. **Phase 3**: Application Layer 리팩토링
4. **Phase 4**: 테스트 작성

---

## 📝 결론

| 항목 | instanceof 분기 | 전략 패턴 | 메타데이터 | 애노테이션 | Spring Event | 커스텀 Publisher |
|------|---------------|----------|----------|----------|-------------|----------------|
| **OCP 준수** | ❌ | ✅ | ⚠️ | ⚠️ | ✅ | ✅ |
| **단순성** | ✅ | ⚠️ | ✅ | ✅ | ✅ | ✅ |
| **타입 안전성** | ✅ | ✅ | ✅ | ⚠️ | ❌ | ✅ |
| **테스트 용이성** | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **클래스 수** | ✅ | ❌ | ✅ | ✅ | ✅ | ⚠️ |
| **계층 분리** | ✅ | ✅ | ❌ | ❌ | ⚠️ | ✅ |

**최종 추천: 커스텀 EventPublisher + 전략 패턴**
