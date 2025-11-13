# Redis Stream Consumer 리팩토링 분석

## 📋 현황 분석

### 1. Consumer 파일 목록
- `RoomEnterStreamConsumer.java` - 방 입장 이벤트 처리
- `CardSelectStreamConsumer.java` - 카드 선택 이벤트 처리

### 2. 코드 구조 비교

#### RoomEnterStreamConsumer
```java
@Component
public class RoomEnterStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {
    // 의존성
    - RoomCommandService
    - MenuCommandService
    - RoomEventWaitManager (비동기 응답 처리)
    - StreamMessageListenerContainer
    - RedisStreamProperties
    - ObjectMapper

    // 메서드
    @PostConstruct registerListener() - 리스너 등록
    onMessage() - 메시지 수신 및 비즈니스 로직 처리 + 비동기 응답
    parseEvent() - JSON 역직렬화
}
```

#### CardSelectStreamConsumer
```java
@Component
public class CardSelectStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {
    // 의존성
    - CardGameCommandService
    - StreamMessageListenerContainer
    - RedisStreamProperties
    - ObjectMapper

    // 메서드
    @PostConstruct registerListener() - 리스너 등록
    onMessage() - 메시지 수신 및 비즈니스 로직 처리 (fire-and-forget)
    parseEvent() - JSON 역직렬화
}
```

### 3. 공통점 식별

✅ **공통 패턴**
1. `StreamListener<String, ObjectRecord<String, String>>` 인터페이스 구현
2. `@PostConstruct registerListener()` 메서드에서 리스너 등록
3. `onMessage()` 메서드에서 메시지 처리
4. `parseEvent()` 메서드에서 JSON 역직렬화
5. 유사한 에러 처리 구조 (try-catch with logging)
6. 공통 의존성: `ObjectMapper`, `RedisStreamProperties`, `StreamMessageListenerContainer`

### 4. 차이점 식별

❌ **주요 차이점**

| 항목 | RoomEnterStreamConsumer | CardSelectStreamConsumer |
|------|-------------------------|--------------------------|
| **처리 방식** | 비동기 응답 처리 (RoomEventWaitManager) | 동기 처리 (fire-and-forget) |
| **비즈니스 로직** | 복잡 (Menu 변환, Room 입장, 여러 서비스 호출) | 단순 (카드 선택) |
| **에러 처리** | notifySuccess/notifyFailure 호출 | 단순 로깅만 |
| **이벤트 타입** | RoomJoinEvent | SelectCardCommandEvent |
| **추가 의존성** | MenuCommandService, RoomEventWaitManager | 없음 |

## 🤔 리팩토링 방안

### 옵션 1: Abstract Base Class (템플릿 메서드 패턴)

공통 로직을 추상 클래스로 추출하고, 차이점은 하위 클래스에서 구현합니다.

```java
@Slf4j
public abstract class AbstractStreamConsumer<T> implements StreamListener<String, ObjectRecord<String, String>> {

    protected final StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;
    protected final RedisStreamProperties properties;
    protected final ObjectMapper objectMapper;

    protected AbstractStreamConsumer(
            StreamMessageListenerContainer<String, ObjectRecord<String, String>> container,
            RedisStreamProperties properties,
            ObjectMapper objectMapper
    ) {
        this.container = container;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void registerListener() {
        container.receive(StreamOffset.fromStart(getStreamKey()), this);
        log.info("{} 스트림 리스너 등록 완료: {}", getConsumerName(), getStreamKey());
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        log.info("{} 메시지 수신: messageId={}", getConsumerName(), message.getId());

        try {
            T event = parseEvent(message);
            processEvent(event, message);
            handleSuccess(event, message);
        } catch (InvalidArgumentException | InvalidStateException e) {
            handleBusinessError(event, message, e);
        } catch (Exception e) {
            handleSystemError(event, message, e);
        }
    }

    protected T parseEvent(ObjectRecord<String, String> message) {
        try {
            String value = message.getValue();
            return objectMapper.readValue(value, getEventType());
        } catch (JsonProcessingException e) {
            log.error("{} 파싱 실패: messageId={}", getConsumerName(), message.getId(), e);
            throw new IllegalArgumentException("이벤트 파싱 실패: " + e.getMessage(), e);
        }
    }

    // 하위 클래스에서 구현해야 하는 추상 메서드
    protected abstract String getStreamKey();
    protected abstract String getConsumerName();
    protected abstract Class<T> getEventType();
    protected abstract void processEvent(T event, ObjectRecord<String, String> message);

    // 선택적으로 오버라이드 가능한 메서드 (기본 구현 제공)
    protected void handleSuccess(T event, ObjectRecord<String, String> message) {
        log.info("{} 처리 성공: messageId={}", getConsumerName(), message.getId());
    }

    protected void handleBusinessError(T event, ObjectRecord<String, String> message, Exception e) {
        log.warn("{} 비즈니스 오류: messageId={}", getConsumerName(), message.getId(), e);
    }

    protected void handleSystemError(T event, ObjectRecord<String, String> message, Exception e) {
        log.error("{} 시스템 오류: messageId={}", getConsumerName(), message.getId(), e);
    }
}
```

**사용 예시 - RoomEnterStreamConsumer**
```java
@Component
public class RoomEnterStreamConsumer extends AbstractStreamConsumer<RoomJoinEvent> {

    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;
    private final RoomEventWaitManager roomEventWaitManager;

    public RoomEnterStreamConsumer(
            RoomCommandService roomCommandService,
            MenuCommandService menuCommandService,
            RoomEventWaitManager roomEventWaitManager,
            @Qualifier("roomEnterStreamContainer") StreamMessageListenerContainer<String, ObjectRecord<String, String>> container,
            RedisStreamProperties properties,
            ObjectMapper objectMapper
    ) {
        super(container, properties, objectMapper);
        this.roomCommandService = roomCommandService;
        this.menuCommandService = menuCommandService;
        this.roomEventWaitManager = roomEventWaitManager;
    }

    @Override
    protected String getStreamKey() {
        return properties.roomJoinKey();
    }

    @Override
    protected String getConsumerName() {
        return "방 입장";
    }

    @Override
    protected Class<RoomJoinEvent> getEventType() {
        return RoomJoinEvent.class;
    }

    @Override
    protected void processEvent(RoomJoinEvent event, ObjectRecord<String, String> message) {
        Menu menu = menuCommandService.convertMenu(
                event.selectedMenuRequest().id(),
                event.selectedMenuRequest().customName()
        );

        Room room = roomCommandService.joinGuest(
                new JoinCode(event.joinCode()),
                new PlayerName(event.guestName()),
                menu,
                event.selectedMenuRequest().temperature()
        );

        roomEventWaitManager.notifySuccess(event.eventId(), room);
    }

    @Override
    protected void handleBusinessError(RoomJoinEvent event, ObjectRecord<String, String> message, Exception e) {
        super.handleBusinessError(event, message, e);
        roomEventWaitManager.notifyFailure(event.eventId(), e);
    }

    @Override
    protected void handleSystemError(RoomJoinEvent event, ObjectRecord<String, String> message, Exception e) {
        super.handleSystemError(event, message, e);
        roomEventWaitManager.notifyFailure(event.eventId(), e);
    }
}
```

**사용 예시 - CardSelectStreamConsumer**
```java
@Component
public class CardSelectStreamConsumer extends AbstractStreamConsumer<SelectCardCommandEvent> {

    private final CardGameCommandService cardGameCommandService;

    public CardSelectStreamConsumer(
            CardGameCommandService cardGameCommandService,
            @Qualifier("cardSelectStreamContainer") StreamMessageListenerContainer<String, ObjectRecord<String, String>> container,
            RedisStreamProperties properties,
            ObjectMapper objectMapper
    ) {
        super(container, properties, objectMapper);
        this.cardGameCommandService = cardGameCommandService;
    }

    @Override
    protected String getStreamKey() {
        return properties.cardGameSelectKey();
    }

    @Override
    protected String getConsumerName() {
        return "카드 선택";
    }

    @Override
    protected Class<SelectCardCommandEvent> getEventType() {
        return SelectCardCommandEvent.class;
    }

    @Override
    protected void processEvent(SelectCardCommandEvent event, ObjectRecord<String, String> message) {
        cardGameCommandService.selectCard(
                new JoinCode(event.joinCode()),
                new PlayerName(event.playerName()),
                event.cardIndex()
        );
    }
}
```

**장점:**
- 공통 로직(파싱, 리스너 등록, 기본 에러 처리)을 한 곳에서 관리
- 새로운 Consumer 추가 시 보일러플레이트 코드 감소
- 일관된 로깅 패턴 유지

**단점:**
- 상속 사용으로 인한 결합도 증가
- RoomEnterStreamConsumer의 특수한 에러 처리(RoomEventWaitManager) 때문에 추상 클래스가 복잡해질 수 있음
- Consumer마다 처리 방식이 다를 경우 템플릿 메서드 패턴의 이점이 감소

### 옵션 2: 유틸리티 클래스로 공통 로직만 추출

parseEvent 로직만 별도 유틸리티로 추출합니다.

```java
@Component
public class StreamEventParser {

    private final ObjectMapper objectMapper;

    public StreamEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T parse(ObjectRecord<String, String> message, Class<T> eventType, String consumerName) {
        try {
            String value = message.getValue();
            return objectMapper.readValue(value, eventType);
        } catch (JsonProcessingException e) {
            log.error("{} 파싱 실패: messageId={}, messageValue={}",
                    consumerName, message.getId(), message.getValue(), e);
            throw new IllegalArgumentException("이벤트 파싱 실패: " + e.getMessage(), e);
        }
    }
}
```

**사용 예시:**
```java
@Component
public class RoomEnterStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {

    private final StreamEventParser eventParser;
    // ... 기존 의존성

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        RoomJoinEvent event = eventParser.parse(message, RoomJoinEvent.class, "방 입장");
        // ... 나머지 로직
    }
}
```

**장점:**
- 최소한의 변경으로 중복 제거
- 상속 없이 합성으로 해결
- 각 Consumer의 독립성 유지

**단점:**
- 제한적인 중복 제거 (파싱 로직만)
- registerListener, onMessage 구조의 중복은 여전히 존재

### 옵션 3: 현상 유지

Consumer는 Producer와 달리 각자의 비즈니스 로직과 에러 처리 방식이 상당히 다릅니다.

**현상 유지 근거:**
1. **비즈니스 로직의 차이**: RoomEnter는 비동기 응답 처리, CardSelect는 fire-and-forget
2. **에러 처리 차이**: RoomEnter는 RoomEventWaitManager를 통한 실패 통지 필요
3. **복잡도 vs 이점**: 추상화로 얻는 이점보다 복잡도 증가가 더 클 수 있음
4. **YAGNI 원칙**: 현재 2개의 Consumer만 존재하고, 각각의 특성이 다름

## 🎯 권장 사항

### 추천: **옵션 2 (유틸리티 클래스)** 또는 **옵션 3 (현상 유지)**

#### 옵션 2를 선택하는 경우:
- 파싱 로직의 중복만 제거
- 각 Consumer의 독립성 유지
- 향후 Consumer가 추가될 경우를 대비한 최소한의 공통화

#### 옵션 3을 선택하는 경우:
- Producer와 달리 Consumer는 비즈니스 로직이 서로 매우 다름
- 추상화의 이점이 명확하지 않음
- 코드 복잡도 증가를 피하고 단순성 유지

### 옵션 1은 권장하지 않는 이유:
1. Consumer가 2개뿐이고, 향후 추가될 가능성도 명확하지 않음
2. RoomEnterStreamConsumer의 특수한 처리(RoomEventWaitManager) 때문에 추상 클래스가 복잡해짐
3. 상속으로 인한 결합도 증가
4. Producer처럼 명확한 중복 패턴이 아님

## 📊 Producer vs Consumer 비교

| 특성 | Producer | Consumer |
|------|----------|----------|
| **중복 정도** | 매우 높음 (거의 동일한 로직) | 중간 (공통 패턴은 있으나 비즈니스 로직 차이 큼) |
| **리팩토링 효과** | 명확함 (61줄 중복 제거) | 제한적 (파싱 로직 정도만) |
| **추상화 복잡도** | 낮음 (단순 합성) | 높음 (템플릿 메서드 또는 상속 필요) |
| **권장 방식** | 합성 패턴 (RedisStreamPublisher) | 유틸리티 추출 또는 현상 유지 |

## 결론

Producer는 명확한 중복이 있어 리팩토링 효과가 컸지만, Consumer는 각자의 비즈니스 로직과 에러 처리 방식이 달라 리팩토링 이점이 제한적입니다.

**최종 권장:**
- **옵션 2 (유틸리티)**: 파싱 로직만 공통화하여 최소한의 중복 제거
- **옵션 3 (현상 유지)**: Consumer가 더 추가되지 않는다면 현재 상태 유지도 합리적

어떤 방식을 선택하시겠습니까?

---

## ✅ 최종 구현 결과

### 선택된 방식: **Handler 패턴 + 제너릭 Consumer 통합**

옵션 1과 옵션 2를 결합한 하이브리드 접근 방식을 채택했습니다.

### 구현 구조

```
┌─────────────────────────────────────────┐
│  GenericStreamConsumer<T>               │  ← 하나로 통합! (메시징 인프라)
│  - 메시지 수신, 파싱, 에러 처리         │
│  - StreamEventHandler<T>에 위임         │
└─────────────────────────────────────────┘
                    │ uses
                    ↓
┌─────────────────────────────────────────┐
│  StreamEventHandler<T> (interface)      │
└─────────────────────────────────────────┘
         ↑                          ↑
         │ implements               │ implements
         │                          │
┌────────────────────┐    ┌─────────────────────┐
│ RoomJoinEvent      │    │ CardSelectEvent     │
│ Handler            │    │ Handler             │
│ (비즈니스 로직)    │    │ (비즈니스 로직)     │
└────────────────────┘    └─────────────────────┘
```

### 생성된 파일

#### 1. 공통 인프라 (global.infra.messaging)
- **StreamEventHandler<T>**: 비즈니스 로직 인터페이스
- **GenericStreamConsumer<T>**: 제너릭 메시징 Consumer

#### 2. 도메인별 Handler
- **RoomJoinEventHandler**: 방 입장 비즈니스 로직
- **CardSelectEventHandler**: 카드 선택 비즈니스 로직

#### 3. Configuration
- **StreamConsumerConfig**: Consumer 빈 등록

### 삭제된 파일
- ~~RoomEnterStreamConsumer.java~~
- ~~CardSelectStreamConsumer.java~~

### 코드 비교

#### Before: 2개의 개별 Consumer (197줄)
```java
// RoomEnterStreamConsumer.java (111줄)
@Component
public class RoomEnterStreamConsumer implements StreamListener<...> {
    private final RoomCommandService roomCommandService;
    private final MenuCommandService menuCommandService;
    private final RoomEventWaitManager roomEventWaitManager;
    // ... 메시징 인프라 코드
    
    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 파싱 로직
        // 비즈니스 로직
        // 에러 처리
    }
}

// CardSelectStreamConsumer.java (86줄)
@Component
public class CardSelectStreamConsumer implements StreamListener<...> {
    private final CardGameCommandService cardGameCommandService;
    // ... 중복된 메시징 인프라 코드
    
    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 중복된 파싱 로직
        // 비즈니스 로직
        // 중복된 에러 처리
    }
}
```

#### After: 1개의 제너릭 Consumer + Handler들 (474줄, 하지만 구조화됨)
```java
// GenericStreamConsumer.java (공통 메시징 인프라)
public class GenericStreamConsumer<T> implements StreamListener<...> {
    private final StreamEventHandler<T> handler;
    // 메시징 인프라만 담당
    
    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        T event = parseEvent(message);
        handler.handle(event); // 비즈니스 로직 위임
    }
}

// RoomJoinEventHandler.java (비즈니스 로직만)
@Component
public class RoomJoinEventHandler implements StreamEventHandler<RoomJoinEvent> {
    @Override
    public void handle(RoomJoinEvent event) {
        // 순수 비즈니스 로직
        roomCommandService.joinGuest(...);
        roomEventWaitManager.notifySuccess(...);
    }
}

// StreamConsumerConfig.java (선언적 설정)
@Configuration
public class StreamConsumerConfig {
    @Bean
    public GenericStreamConsumer<RoomJoinEvent> roomJoinConsumer(...) {
        return new GenericStreamConsumer<>(handler, RoomJoinEvent.class, ...);
    }
}
```

### 개선 효과

#### 1. 관심사 분리 (Separation of Concerns)
- **Consumer**: 메시징 인프라 (수신, 파싱, 에러 처리)
- **Handler**: 비즈니스 로직 (도메인 처리)
- **Configuration**: 와이어링

#### 2. 코드 중복 제거
- **Before**: 2개 Consumer, 메시징 로직 중복
- **After**: 1개 제너릭 Consumer, 중복 완전 제거

#### 3. 테스트 용이성
```java
// Handler 단위 테스트 (메시징 인프라 불필요)
@Test
void 방_입장_처리_성공() {
    RoomJoinEventHandler handler = new RoomJoinEventHandler(
        roomCommandService, menuCommandService, waitManager
    );
    
    RoomJoinEvent event = new RoomJoinEvent(...);
    handler.handle(event); // 순수 비즈니스 로직 테스트
    
    verify(roomCommandService).joinGuest(...);
}
```

#### 4. 확장성
```java
// 새 이벤트 추가 시 - Handler만 구현
@Component
public class GameStartEventHandler implements StreamEventHandler<GameStartEvent> {
    @Override
    public void handle(GameStartEvent event) {
        // 비즈니스 로직
    }
}

// Configuration에서 빈 등록
@Bean
public GenericStreamConsumer<GameStartEvent> gameStartConsumer(...) {
    return new GenericStreamConsumer<>(handler, GameStartEvent.class, ...);
}
```

#### 5. 일관성
- 모든 Stream 이벤트가 동일한 패턴으로 처리
- 로깅, 에러 처리, 파싱 로직 일관성 보장

### Producer와의 일관성

이제 Producer와 Consumer 모두 동일한 설계 원칙을 따릅니다:

| 구분 | Producer | Consumer |
|------|----------|----------|
| **공통 인프라** | RedisStreamPublisher | GenericStreamConsumer<T> |
| **도메인 특화** | XxxStreamProducer | XxxEventHandler |
| **패턴** | 합성 (Composition) | 합성 + 제네릭 |
| **책임** | 발행 로직 / 도메인 로깅 | 메시징 인프라 / 비즈니스 로직 |

### 최종 평가

✅ **Producer처럼 명확한 개선 효과 달성**
- 2개 Consumer → 1개 제너릭 Consumer
- 관심사 명확히 분리
- 테스트 용이성 대폭 향상
- 확장성 및 유지보수성 개선

이전 분석에서 "현상 유지" 또는 "유틸리티만 추출"을 권장했으나,
**Handler 패턴 + 제너릭 Consumer 통합** 접근법이 Producer와 동일한 수준의 명확한 개선을 가져왔습니다.
