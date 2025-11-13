# Stream Producer 중복 제거 - 합성(Composition) 기반 리팩토링

## 📋 현재 상황 분석

### 중복 코드 발견

**RoomEnterStreamProducer**와 **CardSelectStreamProducer**가 거의 동일한 구조를 가지고 있습니다.

#### 공통점 (중복)
```java
// 1. 동일한 의존성
private final StringRedisTemplate stringRedisTemplate;
private final RedisStreamProperties redisStreamProperties;
private final ObjectMapper objectMapper;

// 2. 동일한 발행 로직
String eventJson = objectMapper.writeValueAsString(event);
Record<String, String> record = StreamRecords.newRecord()
    .in(streamKey)  // ⭐ 유일한 차이: streamKey
    .ofObject(eventJson);

String recordId = stringRedisTemplate.opsForStream().add(
    record,
    XAddOptions.maxlen(redisStreamProperties.maxLength()).approximateTrimming(true)
);

// 3. 동일한 예외 처리 패턴
try {
    // 발행 로직
} catch (JsonProcessingException e) {
    // 직렬화 실패
} catch (Exception e) {
    // 발송 실패
}
```

#### 차이점
1. **Stream Key**: `roomJoinKey()` vs `cardGameSelectKey()`
2. **로그 메시지**: 이벤트별로 다른 컨텍스트 정보
3. **이벤트 타입**: `RoomJoinEvent` vs `SelectCardCommandEvent`

---

## 🎯 합성(Composition) 기반 개선 방안

### 방안 1: 공통 RedisStreamPublisher 위임 (추천 ⭐)

#### 설계 개념
```
RoomEnterStreamProducer
  └─ RedisStreamPublisher (합성) ⭐
       ├─ StringRedisTemplate
       ├─ RedisStreamProperties
       └─ ObjectMapper

CardSelectStreamProducer
  └─ RedisStreamPublisher (합성) ⭐
       ├─ StringRedisTemplate
       ├─ RedisStreamProperties
       └─ ObjectMapper
```

#### 구현

**1. 공통 클래스: `RedisStreamPublisher`**
```java
package coffeeshout.global.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

/**
 * Redis Stream 발행을 위한 공통 컴포넌트
 * <p>
 * Stream 발행의 공통 로직을 담당하며, 다양한 Producer에서 합성하여 사용합니다.
 * </p>
 *
 * <p><b>사용 예:</b></p>
 * <ul>
 *   <li>RoomEnterStreamProducer: 방 입장 이벤트 발행</li>
 *   <li>CardSelectStreamProducer: 카드 선택 이벤트 발행</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트를 Redis Stream에 발행합니다.
     *
     * @param event 발행할 이벤트 객체
     * @param streamKey Redis Stream 키
     * @param maxLength Stream 최대 길이
     * @return Redis Record ID
     * @throws RuntimeException 발행 실패 시
     */
    public RecordId publish(Object event, String streamKey, long maxLength) {
        try {
            String eventJson = serializeEvent(event);
            Record<String, String> record = createRecord(eventJson, streamKey);

            return addToStream(record, maxLength);

        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패: streamKey={}, event={}", streamKey, event, e);
            throw new RuntimeException("이벤트 직렬화 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Stream 이벤트 발행 실패: streamKey={}, event={}", streamKey, event, e);
            throw new RuntimeException("Stream 이벤트 발행 실패: " + e.getMessage(), e);
        }
    }

    private String serializeEvent(Object event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    private Record<String, String> createRecord(String eventJson, String streamKey) {
        return StreamRecords.newRecord()
                .in(streamKey)
                .ofObject(eventJson);
    }

    private RecordId addToStream(Record<String, String> record, long maxLength) {
        return stringRedisTemplate.opsForStream().add(
                record,
                XAddOptions.maxlen(maxLength).approximateTrimming(true)
        );
    }
}
```

**2. 개선된 `RoomEnterStreamProducer`**
```java
package coffeeshout.room.infra.messaging;

import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.messaging.RedisStreamPublisher;
import coffeeshout.room.domain.event.RoomJoinEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEnterStreamProducer {

    private final RedisStreamPublisher streamPublisher;  // ⭐ 합성
    private final RedisStreamProperties streamProperties;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        log.info("방 입장 이벤트 발송 시작: eventId={}, joinCode={}, guestName={}",
                event.eventId(), event.joinCode(), event.guestName());

        var recordId = streamPublisher.publish(
                event,
                streamProperties.roomJoinKey(),
                streamProperties.maxLength()
        );

        log.info("방 입장 이벤트 발송 성공: eventId={}, recordId={}, streamKey={}",
                event.eventId(), recordId, streamProperties.roomJoinKey());
    }
}
```

**3. 개선된 `CardSelectStreamProducer`**
```java
package coffeeshout.cardgame.infra.messaging;

import coffeeshout.cardgame.domain.event.SelectCardCommandEvent;
import coffeeshout.global.config.properties.RedisStreamProperties;
import coffeeshout.global.messaging.RedisStreamPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardSelectStreamProducer {

    private final RedisStreamPublisher streamPublisher;  // ⭐ 합성
    private final RedisStreamProperties streamProperties;

    public void broadcastCardSelect(SelectCardCommandEvent event) {
        log.info("카드 선택 이벤트 발송 시작: eventId={}, joinCode={}, playerName={}, cardIndex={}",
                event.eventId(), event.joinCode(), event.playerName(), event.cardIndex());

        var recordId = streamPublisher.publish(
                event,
                streamProperties.cardGameSelectKey(),
                streamProperties.maxLength()
        );

        log.info("카드 선택 이벤트 발송 성공: eventId={}, recordId={}, streamKey={}",
                event.eventId(), recordId, streamProperties.cardGameSelectKey());
    }
}
```

#### 장점
- ✅ **중복 제거**: 공통 로직이 RedisStreamPublisher로 집중
- ✅ **단일 책임**: RedisStreamPublisher는 Stream 발행만 담당
- ✅ **테스트 용이**: 공통 로직을 한 곳에서 테스트 가능
- ✅ **확장 가능**: 새로운 StreamProducer 추가 시 간단
- ✅ **명확한 의존성**: 합성을 통한 명시적 관계

#### 단점
- ⚠️ 로그 메시지가 각 Producer에 분산됨
- ⚠️ 클래스 하나 추가 (복잡도 증가)

---

### 방안 2: 함수형 인터페이스 활용 (고급)

#### 설계
```java
/**
 * 이벤트 발행 콜백 인터페이스
 */
@FunctionalInterface
public interface StreamPublishCallback {
    void onPublish(RecordId recordId, Object event, String streamKey);
}

/**
 * RedisStreamPublisher with callback
 */
@Component
public class RedisStreamPublisher {

    public RecordId publish(
            Object event,
            String streamKey,
            long maxLength,
            StreamPublishCallback beforePublish,  // ⭐ 발행 전 콜백
            StreamPublishCallback afterPublish    // ⭐ 발행 후 콜백
    ) {
        if (beforePublish != null) {
            beforePublish.onPublish(null, event, streamKey);
        }

        // 발행 로직...
        RecordId recordId = addToStream(record, maxLength);

        if (afterPublish != null) {
            afterPublish.onPublish(recordId, event, streamKey);
        }

        return recordId;
    }
}

/**
 * 사용 예
 */
@Component
public class RoomEnterStreamProducer {

    private final RedisStreamPublisher streamPublisher;

    public void broadcastEnterRoom(RoomJoinEvent event) {
        streamPublisher.publish(
            event,
            streamProperties.roomJoinKey(),
            streamProperties.maxLength(),
            (id, e, key) -> log.info("방 입장 이벤트 발송 시작..."),  // before
            (id, e, key) -> log.info("방 입장 이벤트 발송 성공: recordId={}", id)  // after
        );
    }
}
```

#### 장점
- ✅ 유연한 커스터마이징 (로그, 메트릭 등)
- ✅ 함수형 프로그래밍 스타일

#### 단점
- ❌ 복잡도 증가
- ❌ 가독성 저하 (람다 남발)

---

### 방안 3: StreamPublishRequest VO 패턴

#### 설계
```java
/**
 * Stream 발행 요청을 캡슐화하는 VO
 */
@Value
@Builder
public class StreamPublishRequest {
    Object event;
    String streamKey;
    long maxLength;
    String operationName;  // 로그용

    public static StreamPublishRequest of(Object event, String streamKey, long maxLength, String operationName) {
        return StreamPublishRequest.builder()
                .event(event)
                .streamKey(streamKey)
                .maxLength(maxLength)
                .operationName(operationName)
                .build();
    }
}

/**
 * RedisStreamPublisher
 */
@Component
public class RedisStreamPublisher {

    public RecordId publish(StreamPublishRequest request) {
        log.info("{} 이벤트 발송 시작: streamKey={}",
                request.getOperationName(), request.getStreamKey());

        // 발행 로직...
        RecordId recordId = addToStream(record, request.getMaxLength());

        log.info("{} 이벤트 발송 성공: recordId={}, streamKey={}",
                request.getOperationName(), recordId, request.getStreamKey());

        return recordId;
    }
}

/**
 * 사용 예
 */
@Component
public class RoomEnterStreamProducer {

    public void broadcastEnterRoom(RoomJoinEvent event) {
        var request = StreamPublishRequest.of(
            event,
            streamProperties.roomJoinKey(),
            streamProperties.maxLength(),
            "방 입장"
        );

        streamPublisher.publish(request);
    }
}
```

#### 장점
- ✅ 요청 정보를 객체로 캡슐화
- ✅ 확장 가능 (필드 추가 용이)
- ✅ 타입 안전성

#### 단점
- ❌ VO 클래스 추가
- ⚠️ 로그 메시지 커스터마이징 제한

---

## 📊 방안 비교

| 항목 | 방안 1: 위임 | 방안 2: 콜백 | 방안 3: VO |
|------|-----------|-----------|---------|
| **중복 제거** | ✅ 완벽 | ✅ 완벽 | ✅ 완벽 |
| **단순성** | ✅ | ⚠️ | ✅ |
| **로그 커스터마이징** | ⚠️ | ✅ | ⚠️ |
| **타입 안전성** | ✅ | ✅ | ✅ |
| **확장성** | ✅ | ✅ | ✅ |
| **가독성** | ✅ | ❌ | ✅ |
| **학습 곡선** | 낮음 | 높음 | 중간 |

---

## 🎯 최종 추천: 방안 1 (공통 RedisStreamPublisher 위임)

### 추천 이유

1. **단순하고 명확**
   - 추가 개념 없이 단순 위임만 사용
   - 누구나 쉽게 이해 가능

2. **적절한 추상화 수준**
   - 너무 과하지도, 부족하지도 않음
   - 현재 요구사항에 딱 맞음

3. **합성의 장점 활용**
   - 상속 대신 합성 사용
   - 느슨한 결합
   - 테스트 용이

4. **점진적 개선 가능**
   - 나중에 필요하면 콜백이나 VO 패턴으로 확장 가능

---

## 📝 구현 가이드

### Step 1: RedisStreamPublisher 생성
```
backend/src/main/java/coffeeshout/global/messaging/RedisStreamPublisher.java
```

### Step 2: RoomEnterStreamProducer 리팩토링
- RedisStreamPublisher 의존성 주입
- 공통 로직 제거
- streamPublisher.publish() 호출로 단순화

### Step 3: CardSelectStreamProducer 리팩토링
- 동일한 패턴 적용

### Step 4: 테스트
- RedisStreamPublisher 단위 테스트
- 각 Producer 통합 테스트

---

## 🔮 향후 확장

### 새로운 StreamProducer 추가 시

**Before (중복):**
```java
// 50줄의 중복 코드 작성
@Component
public class PaymentStreamProducer {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties redisStreamProperties;
    private final ObjectMapper objectMapper;

    public void broadcastPayment(PaymentEvent event) {
        // 50줄의 중복 로직...
    }
}
```

**After (합성):**
```java
// 10줄로 끝
@Component
@RequiredArgsConstructor
public class PaymentStreamProducer {

    private final RedisStreamPublisher streamPublisher;  // ⭐
    private final RedisStreamProperties streamProperties;

    public void broadcastPayment(PaymentEvent event) {
        log.info("결제 이벤트 발송 시작: {}", event);

        var recordId = streamPublisher.publish(
            event,
            streamProperties.paymentKey(),
            streamProperties.maxLength()
        );

        log.info("결제 이벤트 발송 성공: recordId={}", recordId);
    }
}
```

---

## 💡 추가 고려사항

### 1. 메트릭 수집
```java
@Component
public class RedisStreamPublisher {

    private final MeterRegistry meterRegistry;

    public RecordId publish(Object event, String streamKey, long maxLength) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // 발행 로직...
            RecordId recordId = addToStream(record, maxLength);

            sample.stop(Timer.builder("redis.stream.publish")
                    .tag("streamKey", streamKey)
                    .register(meterRegistry));

            return recordId;
        } catch (Exception e) {
            meterRegistry.counter("redis.stream.publish.error",
                    "streamKey", streamKey).increment();
            throw e;
        }
    }
}
```

### 2. 재시도 로직
```java
@Component
public class RedisStreamPublisher {

    @Retryable(
        value = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    public RecordId publish(Object event, String streamKey, long maxLength) {
        // 발행 로직...
    }
}
```

### 3. 비동기 발행
```java
@Component
public class RedisStreamPublisher {

    @Async("streamPublishExecutor")
    public CompletableFuture<RecordId> publishAsync(
            Object event,
            String streamKey,
            long maxLength
    ) {
        RecordId recordId = publish(event, streamKey, maxLength);
        return CompletableFuture.completedFuture(recordId);
    }
}
```

---

## 🎯 결론

**합성(Composition)을 사용한 공통 RedisStreamPublisher 위임 패턴**이 현재 상황에 가장 적합합니다.

**이유:**
1. ✅ 중복 코드 완전 제거
2. ✅ 단순하고 명확한 구조
3. ✅ 합성의 장점 활용 (느슨한 결합)
4. ✅ 확장 가능하면서도 과하지 않음
5. ✅ 테스트 및 유지보수 용이

다음 단계로 실제 구현을 진행하시겠습니까?
