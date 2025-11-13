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
