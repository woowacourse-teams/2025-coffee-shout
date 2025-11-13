# Room 패키지 Redis 의존성 분리 분석

## 📋 목표
Room 패키지에서 Redis Pub/Sub과 Redis Stream에 대한 직접적인 의존성을 분리하고, application, domain, infra 패키지가 각자의 역할에 맞게 책임을 갖도록 리팩토링

## 🔍 현재 구조 분석

### 1. 패키지 구조
```
room/
├── application/              # 애플리케이션 서비스 계층
│   ├── RoomService.java      ⚠️ Redis 구현체 직접 의존
│   ├── QrCodeService.java    ⚠️ Redis 구현체 직접 의존
│   ├── handler/              # 이벤트 핸들러들
│   └── event/
│       └── RoomEventLocalHandler.java
├── domain/                   # ✅ 도메인 계층 (깨끗함)
│   ├── Room.java
│   ├── event/
│   │   ├── RoomBaseEvent.java
│   │   ├── RoomCreateEvent.java
│   │   ├── RoomJoinEvent.java
│   │   └── ...
│   └── service/
├── infra/                    # 인프라 계층
│   ├── messaging/            # Redis 구현체
│   │   ├── RoomEventPublisher.java       # Pub/Sub
│   │   ├── RoomEnterStreamProducer.java  # Stream
│   │   ├── RoomEventSubscriber.java
│   │   └── RoomEnterStreamConsumer.java
│   └── persistence/          # JPA 구현
└── ui/                       # 프레젠테이션 계층
```

### 2. 현재 의존성 흐름

```
┌─────────────────────────────────────────┐
│        Application Layer                │
│   (RoomService, QrCodeService)          │
│                                         │
│   - RoomEventPublisher      ⚠️ 직접 의존│
│   - RoomEnterStreamProducer ⚠️ 직접 의존│
└────────────┬────────────────────────────┘
             │
             ↓
┌─────────────────────────────────────────┐
│          Infra Layer                    │
│                                         │
│  RoomEventPublisher                     │
│    ├─ RedisTemplate<String, Object>    │
│    └─ TopicManager                      │
│                                         │
│  RoomEnterStreamProducer                │
│    ├─ StringRedisTemplate               │
│    ├─ RedisStreamProperties             │
│    └─ ObjectMapper                      │
└─────────────────────────────────────────┘
```

### 3. 문제점 상세 분석

#### 🔴 A. 강한 결합 (Tight Coupling)
**파일: RoomService.java (Line 58, 61)**
```java
@Service
public class RoomService {
    private final RoomEventPublisher roomEventPublisher;        // ⚠️ Infra 구현체
    private final RoomEnterStreamProducer roomEnterStreamProducer; // ⚠️ Infra 구현체

    public Room createRoom(...) {
        roomEventPublisher.publishEvent(event);  // Redis에 강하게 결합
    }
}
```

**문제:**
- Application Layer가 Infra Layer의 구체적인 구현에 직접 의존
- Redis를 Kafka, RabbitMQ 등으로 교체 시 Application 코드 변경 필요
- 의존성 방향이 역전되지 않음 (DIP 위반)

#### 🔴 B. 테스트 어려움
**문제:**
```java
@Test
void createRoomTest() {
    // ❌ 실제 Redis 필요하거나 복잡한 Mock 설정 필요
    // ❌ RoomEventPublisher를 Mock으로 만들어야 함
}
```

#### 🔴 C. 레이어 책임 불명확
**현재:**
- Application Layer: 비즈니스 로직 + **메시징 기술 선택**
- Infra Layer: Redis 구현

**문제:**
- 메시징 기술 선택은 Infra의 관심사인데 Application이 알고 있음
- 도메인 이벤트 발행과 실제 메시징 구현이 혼재

## 🎯 개선 방안

### 1. 의존성 역전 원칙 적용 (Dependency Inversion Principle)

#### A. 인터페이스 정의 (Domain/Application Layer)

**새 파일: `room/domain/event/EventPublisher.java`**
```java
package coffeeshout.room.domain.event;

/**
 * 도메인 이벤트 발행 인터페이스
 * - 도메인/애플리케이션 계층에서 정의
 * - 구체적인 메시징 기술에 독립적
 */
public interface EventPublisher {

    /**
     * 이벤트를 발행한다
     * @param event 발행할 이벤트
     * @param <T> 이벤트 타입 (RoomBaseEvent를 구현한 타입)
     */
    <T extends RoomBaseEvent> void publish(T event);
}
```

**새 파일: `room/application/port/RoomJoinEventPublisher.java`**
```java
package coffeeshout.room.application.port;

import coffeeshout.room.domain.event.RoomJoinEvent;

/**
 * 방 입장 이벤트 발행 포트 (헥사고날 아키텍처)
 * - Application Layer에서 정의하는 출력 포트
 * - 순차 처리가 필요한 방 입장 이벤트 전용
 */
public interface RoomJoinEventPublisher {

    /**
     * 방 입장 이벤트를 순차적으로 발행한다
     * (Redis Stream 등 순서 보장이 필요한 경우)
     */
    void publishRoomJoinEvent(RoomJoinEvent event);
}
```

#### B. 구현체 이동 (Infra Layer)

**리팩토링: `room/infra/messaging/RedisEventPublisher.java`**
```java
package coffeeshout.room.infra.messaging;

import coffeeshout.room.domain.event.EventPublisher;
import coffeeshout.room.domain.event.RoomBaseEvent;
// ... imports

/**
 * Redis Pub/Sub 기반 이벤트 발행 구현체
 * - Infra Layer의 구현
 * - Domain의 EventPublisher 인터페이스 구현
 */
@Component
public class RedisEventPublisher implements EventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TopicManager topicManager;

    @Override
    public <T extends RoomBaseEvent> void publish(T event) {
        String topic = topicManager.getTopic(EventTopicRegistry.ROOM).getTopic();
        redisTemplate.convertAndSend(topic, event);
        log.info("Redis Pub/Sub로 이벤트 발행: {}", event);
    }
}
```

**리팩토링: `room/infra/messaging/RedisStreamRoomJoinEventPublisher.java`**
```java
package coffeeshout.room.infra.messaging;

import coffeeshout.room.application.port.RoomJoinEventPublisher;
import coffeeshout.room.domain.event.RoomJoinEvent;
// ... imports

/**
 * Redis Stream 기반 방 입장 이벤트 발행 구현체
 * - Application의 RoomJoinEventPublisher 포트 구현
 * - 순서 보장이 필요한 방 입장 이벤트 전용
 */
@Component
public class RedisStreamRoomJoinEventPublisher implements RoomJoinEventPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public void publishRoomJoinEvent(RoomJoinEvent event) {
        String eventJson = objectMapper.writeValueAsString(event);
        Record<String, String> record = StreamRecords.newRecord()
            .in(properties.roomJoinKey())
            .ofObject(eventJson);

        stringRedisTemplate.opsForStream().add(record,
            XAddOptions.maxlen(properties.maxLength()).approximateTrimming(true));

        log.info("Redis Stream으로 방 입장 이벤트 발행: {}", event);
    }
}
```

#### C. Application Layer 수정

**리팩토링: `room/application/RoomService.java`**
```java
@Service
public class RoomService {

    // ✅ 인터페이스에만 의존
    private final EventPublisher eventPublisher;
    private final RoomJoinEventPublisher roomJoinEventPublisher;

    // 더 이상 Redis 구현체를 모름!

    @Transactional
    public Room createRoom(String hostName, SelectedMenuRequest request) {
        // ... 비즈니스 로직 ...

        RoomCreateEvent event = new RoomCreateEvent(...);
        eventPublisher.publish(event);  // ✅ 추상화된 인터페이스 사용

        return room;
    }

    public CompletableFuture<Room> enterRoomAsync(...) {
        // ... 비즈니스 로직 ...

        RoomJoinEvent event = new RoomJoinEvent(...);
        roomJoinEventPublisher.publishRoomJoinEvent(event);  // ✅ 포트 사용

        return future;
    }
}
```

### 2. 개선된 의존성 흐름

```
┌─────────────────────────────────────────┐
│         Domain Layer                    │
│                                         │
│   <<interface>>                         │
│   EventPublisher                        │
│   RoomBaseEvent                         │
└────────────┬────────────────────────────┘
             ↑ 의존성 역전!
             │
┌────────────┴────────────────────────────┐
│      Application Layer                  │
│                                         │
│  <<interface>>                          │
│  RoomJoinEventPublisher (Port)          │
│                                         │
│  RoomService                            │
│    └─ EventPublisher        ✅ 인터페이스│
│    └─ RoomJoinEventPublisher ✅ 포트    │
└────────────┬────────────────────────────┘
             ↑ 의존성 역전!
             │
┌────────────┴────────────────────────────┐
│         Infra Layer                     │
│                                         │
│  RedisEventPublisher                    │
│    implements EventPublisher            │
│    ├─ RedisTemplate                     │
│    └─ TopicManager                      │
│                                         │
│  RedisStreamRoomJoinEventPublisher      │
│    implements RoomJoinEventPublisher    │
│    ├─ StringRedisTemplate               │
│    └─ RedisStreamProperties             │
└─────────────────────────────────────────┘
```

### 3. 레이어별 책임 재정의

#### 🟢 Domain Layer
**책임:**
- 도메인 이벤트 정의 (RoomBaseEvent, RoomCreateEvent 등)
- 이벤트 발행 인터페이스 정의 (EventPublisher)
- 비즈니스 규칙

**파일:**
- `domain/event/EventPublisher.java` ⭐ NEW
- `domain/event/RoomBaseEvent.java`
- `domain/event/*Event.java`

#### 🟢 Application Layer
**책임:**
- 유스케이스 구현 (방 생성, 방 입장 등)
- 출력 포트 정의 (Port Interface)
- 트랜잭션 관리
- 도메인 이벤트 발행 (인터페이스를 통해)

**파일:**
- `application/RoomService.java`
- `application/port/RoomJoinEventPublisher.java` ⭐ NEW
- `application/handler/*EventHandler.java`

#### 🟢 Infra Layer
**책임:**
- 포트 인터페이스 구현 (Adapter)
- Redis, Kafka 등 메시징 기술 구현
- 메시징 설정 및 에러 처리

**파일:**
- `infra/messaging/RedisEventPublisher.java` ⭐ RENAMED
- `infra/messaging/RedisStreamRoomJoinEventPublisher.java` ⭐ RENAMED
- `infra/messaging/*Consumer.java`
- `infra/messaging/*Subscriber.java`

## 📊 개선 전후 비교

### Before (현재)
```java
// ❌ Application이 Infra 구현체에 강하게 결합
@Service
public class RoomService {
    private final RoomEventPublisher roomEventPublisher;  // Redis 구현체
}
```

### After (개선)
```java
// ✅ Application이 추상화된 인터페이스에만 의존
@Service
public class RoomService {
    private final EventPublisher eventPublisher;  // 인터페이스
}
```

## 🎁 기대 효과

### 1. ✅ 기술 독립성
- Redis를 다른 메시징 시스템으로 교체 가능
- Application 코드 변경 없이 Infra만 교체

### 2. ✅ 테스트 용이성
```java
@Test
void createRoomTest() {
    // ✅ 간단한 Mock으로 테스트 가능
    EventPublisher mockPublisher = mock(EventPublisher.class);
    RoomService service = new RoomService(mockPublisher, ...);

    service.createRoom(...);

    verify(mockPublisher).publish(any(RoomCreateEvent.class));
}
```

### 3. ✅ 명확한 책임 분리
- Domain: 이벤트 정의
- Application: 유스케이스 + 포트 정의
- Infra: 구현

### 4. ✅ 확장 가능성
```java
// 새로운 메시징 시스템 추가 시
@Component
public class KafkaEventPublisher implements EventPublisher {
    // Kafka 구현
}

// Application 코드는 전혀 변경 없음!
```

## 📝 리팩토링 체크리스트

### Phase 1: 인터페이스 정의
- [ ] `domain/event/EventPublisher.java` 생성
- [ ] `application/port/RoomJoinEventPublisher.java` 생성

### Phase 2: 구현체 리팩토링
- [ ] `RoomEventPublisher` → `RedisEventPublisher`로 이름 변경
- [ ] `EventPublisher` 인터페이스 구현 추가
- [ ] `RoomEnterStreamProducer` → `RedisStreamRoomJoinEventPublisher`로 변경
- [ ] `RoomJoinEventPublisher` 포트 구현 추가

### Phase 3: Application Layer 수정
- [ ] `RoomService` 의존성을 인터페이스로 변경
- [ ] `QrCodeService` 의존성 확인 및 변경

### Phase 4: 테스트 작성
- [ ] 인터페이스 기반 단위 테스트 작성
- [ ] Redis 통합 테스트 작성

### Phase 5: 문서화
- [ ] 아키텍처 결정 기록 (ADR) 작성
- [ ] 새로운 이벤트 추가 가이드 작성

## 🔗 참고 자료
- Clean Architecture - Robert C. Martin
- Hexagonal Architecture (Ports and Adapters)
- Domain-Driven Design - Eric Evans
