# Room 패키지 Redis 의존성 분리 리팩토링 분석 (개선)

## 📋 목표
Room 패키지에서 Redis Pub/Sub과 Redis Stream에 대한 직접적인 의존성을 분리하고, application, domain, infra 패키지가 각자의 역할에 맞게 책임을 갖도록 리팩토링

## 🎯 핵심 원칙

**"클라이언트(Application Layer)는 구현 디테일(Pub/Sub vs Stream)을 몰라야 한다"**

- Application은 "이벤트를 발행한다"는 목적만 알면 됨
- Pub/Sub을 쓸지 Stream을 쓸지는 Infra Layer의 구현 디테일

## 🔍 현재 구조 분석

### 1. 현재 문제

**Application Layer가 Redis 구현체를 직접 의존:**
```java
@Service
public class RoomService {
    private final RoomEventPublisher roomEventPublisher;        // ⚠️ Redis Pub/Sub 구현체
    private final RoomEnterStreamProducer roomEnterStreamProducer; // ⚠️ Redis Stream 구현체

    public Room createRoom(...) {
        roomEventPublisher.publishEvent(event);  // Pub/Sub 사용
    }

    public CompletableFuture<Room> enterRoomAsync(...) {
        roomEnterStreamProducer.broadcastEnterRoom(event);  // Stream 사용
    }
}
```

**문제점:**
- Application이 메시징 메커니즘(Pub/Sub vs Stream)을 직접 선택
- 구현 기술 교체 시 Application 코드 변경 필요
- 두 개의 Publisher 관리 = 복잡성 증가

## 🎯 개선 방안: 단일 인터페이스 설계

### 1. 도메인/애플리케이션에서 인터페이스 정의

**파일: `room/domain/event/EventPublisher.java`**
```java
package coffeeshout.room.domain.event;

/**
 * 도메인 이벤트 발행 인터페이스
 * - 도메인/애플리케이션 계층에서 정의
 * - 구체적인 메시징 기술(Pub/Sub, Stream 등)에 독립적
 */
public interface EventPublisher {

    /**
     * 이벤트를 발행한다
     * - 이벤트 타입에 따라 적절한 메커니즘으로 발행됨 (구현체가 결정)
     *
     * @param event 발행할 이벤트
     * @param <T> 이벤트 타입 (RoomBaseEvent를 구현한 타입)
     */
    <T extends RoomBaseEvent> void publish(T event);
}
```

### 2. Infra Layer에서 구현 - 타입별 분기

**파일: `room/infra/messaging/RedisEventPublisher.java`**
```java
package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.config.redis.EventTopicRegistry;
import coffeeshout.global.config.redis.TopicManager;
import coffeeshout.room.domain.event.EventPublisher;
import coffeeshout.room.domain.event.RoomBaseEvent;
import coffeeshout.room.domain.event.RoomJoinEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 이벤트 발행 구현체
 * - 이벤트 타입에 따라 적절한 Redis 메커니즘 선택
 *   - RoomJoinEvent: Redis Stream (순서 보장 필요)
 *   - 기타 이벤트: Redis Pub/Sub (브로드캐스트)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEventPublisher implements EventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final TopicManager topicManager;
    private final RedisStreamProperties streamProperties;
    private final ObjectMapper objectMapper;

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        try {
            if (event instanceof RoomJoinEvent) {
                // RoomJoinEvent는 순서 보장이 필요하므로 Stream 사용
                publishToStream((RoomJoinEvent) event);
            } else {
                // 나머지 이벤트는 Pub/Sub으로 브로드캐스트
                publishToPubSub(event);
            }

            log.info("이벤트 발행 완료: eventType={}, eventId={}",
                    event.eventType(), event.eventId());

        } catch (Exception e) {
            log.error("이벤트 발행 실패: eventType={}, eventId={}",
                    event.eventType(), event.eventId(), e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    /**
     * Redis Pub/Sub으로 이벤트 발행
     * - 즉시 브로드캐스트
     * - 구독자가 없어도 정상 동작
     */
    private void publishToPubSub(RoomBaseEvent event) {
        String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
        redisTemplate.convertAndSend(topic, event);

        log.debug("Redis Pub/Sub로 이벤트 발행: topic={}, eventType={}",
                topic, event.eventType());
    }

    /**
     * Redis Stream으로 이벤트 발행
     * - 순서 보장
     * - 컨슈머 그룹을 통한 정확히 한 번(exactly-once) 처리
     */
    private void publishToStream(RoomJoinEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            Record<String, String> record = StreamRecords.newRecord()
                    .in(streamProperties.roomJoinKey())
                    .ofObject(eventJson);

            String recordId = stringRedisTemplate.opsForStream().add(record);

            log.debug("Redis Stream으로 이벤트 발행: streamKey={}, recordId={}, eventId={}",
                    streamProperties.roomJoinKey(), recordId, event.eventId());

        } catch (Exception e) {
            log.error("Redis Stream 이벤트 발행 실패: eventId={}", event.eventId(), e);
            throw new RuntimeException("Stream 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }
}
```

### 3. Application Layer 수정 - 단일 인터페이스만 의존

**파일: `room/application/RoomService.java`**
```java
@Service
@RequiredArgsConstructor
public class RoomService {

    // ✅ 하나의 인터페이스만 의존
    private final EventPublisher eventPublisher;

    // ❌ 제거됨
    // private final RoomEventPublisher roomEventPublisher;
    // private final RoomEnterStreamProducer roomEnterStreamProducer;

    @Transactional
    public Room createRoom(String hostName, SelectedMenuRequest request) {
        // 비즈니스 로직...

        RoomCreateEvent event = new RoomCreateEvent(...);
        eventPublisher.publish(event);  // ✅ 단순하게 발행만!

        return room;
    }

    public CompletableFuture<Room> enterRoomAsync(...) {
        // 비즈니스 로직...

        RoomJoinEvent event = new RoomJoinEvent(...);
        eventPublisher.publish(event);  // ✅ 동일한 인터페이스 사용!

        return future;
    }
}
```

## 📊 개선된 아키텍처

### Before (기존 제안 - 복잡)
```
Application Layer
  ├─ EventPublisher (Pub/Sub용)          ❌ 복잡
  └─ RoomJoinEventPublisher (Stream용)   ❌ 복잡
         ↑
Infra Layer
  ├─ RedisEventPublisher
  └─ RedisStreamRoomJoinEventPublisher
```

### After (개선 - 단순)
```
Application Layer
  └─ EventPublisher (단일 인터페이스)    ✅ 단순!
         ↑
Infra Layer
  └─ RedisEventPublisher
       ├─ publishToPubSub()    (내부 메서드)
       └─ publishToStream()    (내부 메서드)
```

## 🎁 개선 효과

### 1. ✅ 단순성
```java
// Before: 두 개의 Publisher
eventPublisher.publish(event1);
roomJoinEventPublisher.publishRoomJoinEvent(event2);

// After: 하나의 Publisher
eventPublisher.publish(event1);
eventPublisher.publish(event2);
```

### 2. ✅ 관심사 분리
- **Application**: "이벤트를 발행한다"만 알면 됨
- **Infra**: 이벤트 타입에 따라 Pub/Sub vs Stream 선택

### 3. ✅ 확장 가능성
```java
// 새로운 이벤트 타입 추가 시
@Override
public <T extends RoomBaseEvent> void publish(T event) {
    if (event instanceof RoomJoinEvent) {
        publishToStream((RoomJoinEvent) event);
    } else if (event instanceof PaymentEvent) {  // ⭐ 새로운 타입
        publishToKafka((PaymentEvent) event);     // ⭐ 다른 메커니즘
    } else {
        publishToPubSub(event);
    }
}
```

### 4. ✅ 테스트 용이성
```java
@Test
void roomServiceTest() {
    // ✅ 하나의 Mock만 필요
    EventPublisher mockPublisher = mock(EventPublisher.class);
    RoomService service = new RoomService(mockPublisher, ...);

    service.createRoom(...);
    service.enterRoomAsync(...);

    // 모든 이벤트가 같은 인터페이스로 발행됨
    verify(mockPublisher, times(2)).publish(any(RoomBaseEvent.class));
}
```

## 🤔 대안: 전략 패턴 + 팩토리

더 확장 가능한 설계가 필요하다면:

**파일: `room/infra/messaging/PublishStrategy.java`**
```java
public interface PublishStrategy {
    boolean supports(RoomBaseEvent event);
    void publish(RoomBaseEvent event);
}

@Component
class PubSubPublishStrategy implements PublishStrategy {
    public boolean supports(RoomBaseEvent event) {
        return !(event instanceof RoomJoinEvent);
    }

    public void publish(RoomBaseEvent event) {
        // Pub/Sub 로직
    }
}

@Component
class StreamPublishStrategy implements PublishStrategy {
    public boolean supports(RoomBaseEvent event) {
        return event instanceof RoomJoinEvent;
    }

    public void publish(RoomBaseEvent event) {
        // Stream 로직
    }
}

@Component
class RedisEventPublisher implements EventPublisher {
    private final List<PublishStrategy> strategies;

    public void publish(RoomBaseEvent event) {
        strategies.stream()
            .filter(s -> s.supports(event))
            .findFirst()
            .orElseThrow()
            .publish(event);
    }
}
```

**장점**: 새로운 전략 추가 시 기존 코드 수정 불필요 (OCP)
**단점**: 오버엔지니어링 가능성 (현재는 2가지만 있음)

## 📝 리팩토링 체크리스트 (개선)

### Phase 1: 인터페이스 정의
- [ ] `domain/event/EventPublisher.java` 생성 (단일 인터페이스)

### Phase 2: 구현체 통합
- [ ] `RoomEventPublisher` + `RoomEnterStreamProducer` → `RedisEventPublisher`로 통합
- [ ] 타입별 분기 로직 구현 (publishToPubSub, publishToStream)
- [ ] 기존 Consumer/Subscriber는 유지

### Phase 3: Application Layer 수정
- [ ] `RoomService` 의존성을 `EventPublisher` 하나로 변경
- [ ] `QrCodeService` 의존성 확인 및 변경
- [ ] 모든 `publish()` 호출을 단일 인터페이스로 통일

### Phase 4: 테스트 작성
- [ ] 단일 인터페이스 기반 단위 테스트
- [ ] 타입별 분기 로직 테스트
- [ ] Redis 통합 테스트

### Phase 5: 정리
- [ ] 기존 파일 삭제 (`RoomEventPublisher.java`, `RoomEnterStreamProducer.java`)
- [ ] 문서 업데이트

## 💡 추가 고려사항

### 1. 이벤트 메타데이터 방식 (선택적)

이벤트 타입으로 분기하는 대신 메타데이터 사용:

```java
public interface RoomBaseEvent {
    String eventId();
    Instant timestamp();
    RoomEventType eventType();

    // ⭐ 발행 요구사항을 메타데이터로 표현
    default PublishRequirement getPublishRequirement() {
        return PublishRequirement.BROADCAST;  // 기본값
    }
}

public record RoomJoinEvent(...) implements RoomBaseEvent {
    @Override
    public PublishRequirement getPublishRequirement() {
        return PublishRequirement.ORDERED;  // 순서 보장 필요
    }
}

enum PublishRequirement {
    BROADCAST,    // Pub/Sub
    ORDERED,      // Stream
    PERSISTENT    // 다른 메커니즘
}
```

**장점**: instanceof 체크 제거, 메타데이터 기반 확장
**단점**: 도메인 이벤트에 인프라 관련 메타데이터 추가

### 2. 현재 추천 방식

**타입별 분기 (instanceof 체크)**가 현재로서는 가장 적절:
- 간단하고 명확
- 이벤트가 많지 않음 (10개 미만)
- 성능 영향 미미
- 메타데이터 오염 없음

## 🎯 결론

**단일 EventPublisher 인터페이스를 사용하고, 구현체 내부에서 타입별로 적절한 메커니즘을 선택하는 방식이 최선**

**이유:**
1. Application Layer는 구현 디테일을 몰라도 됨
2. 단순하고 테스트하기 쉬움
3. 확장 가능하면서도 과도한 추상화 없음
4. 의존성 역전 원칙(DIP) 준수
