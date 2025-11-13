# RoomService 책임 분리 및 개선 방안 분석

## 1. 현재 구조 분석

### 1.1 계층 구조

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  - RoomController (REST API)                                 │
│  - RoomWebSocketController (WebSocket)                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│  - RoomService                                               │
│    • createRoom()                                            │
│    • enterRoomAsync() (비동기)                               │
│    • changePlayerReadyState()                                │
│    • changePlayerReadyStateInternal()  ← Handler 전용        │
│    • getPlayersInternal()              ← Handler 전용        │
│    • updateMiniGamesInternal()         ← Handler 전용        │
│    • removePlayer()                                          │
│    • kickPlayer()                                            │
│    • showRoulette()                                          │
│    • saveRoomEntity()                  ← JPA 직접 접근       │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────────┐
│  Domain  │  │ Infra    │  │   Handler    │
│ Services │  │ Layer    │  │  (Pub/Sub)   │
└──────────┘  └──────────┘  └──────────────┘
                                    │
                                    ▼
                            RoomService.xxxInternal()
                                    │
                                    ▼
                            Domain Services
```

### 1.2 RoomService 책임 혼재

**RoomService.java** (323줄)

#### A. Application Service 책임
```java
// 1. 비동기 처리
public CompletableFuture<Room> enterRoomAsync(...)
private <T> CompletableFuture<T> processEventAsync(...)

// 2. 이벤트 발행
roomEventPublisher.publish(event);

// 3. 트랜잭션 경계
@Transactional
public Room createRoom(...)
```

#### B. Domain Service 위임
```java
// RoomQueryService 호출
final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));

// RoomCommandService 호출
roomCommandService.save(room);
roomCommandService.joinGuest(...);
```

#### C. Infrastructure 직접 접근 ⚠️ 문제
```java
private final RoomJpaRepository roomJpaRepository;  // 직접 주입

public void saveRoomEntity(String joinCodeValue) {
    final RoomEntity roomEntity = new RoomEntity(joinCodeValue);
    roomJpaRepository.save(roomEntity);
}
```

#### D. Handler 전용 Internal 메서드 ⚠️ 문제
```java
// Handler에서 호출되는 메서드들
public List<Player> changePlayerReadyStateInternal(String joinCode, String playerName, Boolean isReady)
public List<Player> getPlayersInternal(String joinCode)
public List<MiniGameType> updateMiniGamesInternal(String joinCode, String hostName, List<MiniGameType> miniGameTypes)
```

#### E. Controller 전용 메서드
```java
public Room createRoom(String hostName, SelectedMenuRequest selectedMenuRequest)
public CompletableFuture<Room> enterRoomAsync(...)
public boolean roomExists(String joinCode)
public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady)
```

### 1.3 Handler들의 RoomService 호출 패턴

| Handler | 호출 메서드 | 역할 |
|---------|------------|------|
| **RoomCreateEventHandler** | ❌ 호출 안 함 | RoomCommandService 직접 호출 ✅ |
| **RoomJoinStreamHandler** | ❌ 호출 안 함 | RoomCommandService 직접 호출 ✅ |
| **PlayerReadyEventHandler** | `roomService.changePlayerReadyStateInternal()` | 준비 상태 변경 + WebSocket 발송 |
| **PlayerKickEventHandler** | `roomService.removePlayer()` <br> `roomService.getPlayersInternal()` | 플레이어 강퇴 + 목록 조회 |
| **PlayerListUpdateEventHandler** | `roomService.getPlayersInternal()` | 플레이어 목록 조회 |
| **MiniGameSelectEventHandler** | `roomService.updateMiniGamesInternal()` | 미니게임 선택 |
| **RouletteShowEventHandler** | `roomService.showRoulette()` | 룰렛 표시 상태 변경 |
| **RouletteSpinEventHandler** | ❌ 호출 안 함 | RoulettePersistenceService 직접 호출 ✅ |

**문제점**:
- ✅ **일부 Handler는 이미 Domain Service 직접 호출** (RoomCreateEventHandler, RoomJoinStreamHandler, RouletteSpinEventHandler)
- ⚠️ **나머지 Handler는 RoomService의 Internal 메서드 호출**
- ⚠️ **일관성 없는 계층 구조**

## 2. 핵심 문제점

### 2.1 불필요한 레이어 순회 (비효율적)

```
현재 구조:
Controller → RoomService.createRoom()
              └─→ Event 발행
                  └─→ Handler
                      └─→ RoomService.xxxInternal()  ⚠️ 다시 RoomService로
                          └─→ RoomCommandService

개선 필요:
Controller → RoomService
              └─→ Event 발행
                  └─→ Handler
                      └─→ RoomCommandService (직접)  ✅
```

**비효율성**:
1. Handler가 RoomService를 다시 호출하는 것은 불필요한 레이어 순회
2. RoomService는 이미 Controller 전용 메서드와 Handler 전용 메서드 혼재
3. "Internal"이라는 네이밍으로 Handler 전용임을 암시하지만, 구조적으로 비효율적

### 2.2 계층 분리 원칙 위반

**DDD 계층 구조**:
```
Presentation → Application → Domain → Infrastructure
```

**현재 문제**:
```
RoomService (Application)
├─ RoomJpaRepository (Infrastructure) 직접 주입  ⚠️
├─ RoomCommandService (Domain) 호출
└─ RoomQueryService (Domain) 호출
```

**위반 사항**:
- Application Layer가 Infrastructure Layer(JPA Repository)를 직접 접근
- `saveRoomEntity()` 메서드가 JPA 엔티티를 직접 저장

### 2.3 책임 불명확 (SRP 위반)

**RoomService가 담당하는 책임**:
1. 비동기 처리 (CompletableFuture)
2. 이벤트 발행
3. 트랜잭션 경계 관리
4. Domain Service 오케스트레이션
5. JPA 직접 접근 ⚠️
6. Handler 전용 로직 ⚠️
7. Controller 전용 로직

→ **단일 책임 원칙(SRP) 위반**: 너무 많은 책임

### 2.4 Internal 메서드의 중복

```java
// Controller용
public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
    return changePlayerReadyStateInternal(joinCode, playerName, isReady);
}

// Handler용
public List<Player> changePlayerReadyStateInternal(String joinCode, String playerName, Boolean isReady) {
    final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
    final Player player = room.findPlayer(new PlayerName(playerName));

    if (player.getPlayerType() == PlayerType.HOST) {
        return room.getPlayers();
    }

    player.updateReadyState(isReady);
    roomCommandService.save(room);
    return room.getPlayers();
}
```

**문제점**:
- `changePlayerReadyState()`는 단순히 `changePlayerReadyStateInternal()` 호출
- 중복된 메서드가 존재하는 이유가 Controller/Handler 구분뿐
- Internal 메서드는 RoomService에 있을 필요 없음 (Domain Service로 이동 가능)

### 2.5 영속성 처리 분산

**JPA 저장 로직이 여러 곳에 분산**:

1. **RoomService.saveRoomEntity()**
   ```java
   public void saveRoomEntity(String joinCodeValue) {
       final RoomEntity roomEntity = new RoomEntity(joinCodeValue);
       roomJpaRepository.save(roomEntity);
   }
   ```

2. **RouletteService** (Infrastructure Layer)
   ```java
   @Transactional
   public void updateRoomStatusToRoulette(String joinCode) {
       final RoomEntity roomEntity = getRoomEntity(joinCode);
       roomEntity.updateRoomStatus(RoomState.ROULETTE);
   }
   ```

3. **RoulettePersistenceService** (Infrastructure Layer)
   ```java
   @RedisLock(...)
   public void saveRoomStatus(RouletteShowEvent event) {
       rouletteService.updateRoomStatusToRoulette(event.joinCode());
   }
   ```

**문제점**:
- JPA 영속성 로직이 Application, Infrastructure 계층에 분산
- 일관성 없는 구조

## 3. 개선 방안

### 옵션 1: Handler가 Domain Service 직접 호출 ⭐ 추천

**구조**:
```
Controller → RoomApplicationService
              └─→ Event 발행
                  └─→ Handler
                      └─→ RoomCommandService (직접 호출)
                      └─→ RoomQueryService (직접 호출)
```

**변경 사항**:

#### A. RoomService → RoomApplicationService 리네이밍
- Application Service의 역할을 명확히 표현
- Controller 전용 서비스

#### B. Internal 메서드 제거
```java
// 삭제
changePlayerReadyStateInternal()
getPlayersInternal()
updateMiniGamesInternal()
```

#### C. Handler가 Domain Service 직접 호출
```java
// Before
@Component
public class PlayerReadyEventHandler {
    private final RoomService roomService;

    public void handle(PlayerReadyEvent event) {
        final List<Player> players = roomService.changePlayerReadyStateInternal(...);
    }
}

// After
@Component
public class PlayerReadyEventHandler {
    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;

    public void handle(PlayerReadyEvent event) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
        final Player player = room.findPlayer(new PlayerName(event.playerName()));

        if (player.getPlayerType() != PlayerType.HOST) {
            player.updateReadyState(event.isReady());
            roomCommandService.save(room);
        }
    }
}
```

#### D. JPA 직접 접근 제거
```java
// Before
@Service
public class RoomApplicationService {
    private final RoomJpaRepository roomJpaRepository;  // 제거

    public void saveRoomEntity(String joinCodeValue) {
        final RoomEntity roomEntity = new RoomEntity(joinCodeValue);
        roomJpaRepository.save(roomEntity);
    }
}

// After
// RoomPersistenceService로 이동 (Infrastructure Layer)
@Component
public class RoomPersistenceService {
    private final RoomJpaRepository roomJpaRepository;

    public void saveRoomSession(String joinCodeValue) {
        final RoomEntity roomEntity = new RoomEntity(joinCodeValue);
        roomJpaRepository.save(roomEntity);
    }
}
```

#### E. Controller용 메서드만 유지
```java
@Service
public class RoomApplicationService {
    // Controller 전용 메서드만 유지
    public Room createRoom(String hostName, SelectedMenuRequest selectedMenuRequest)
    public CompletableFuture<Room> enterRoomAsync(...)
    public boolean roomExists(String joinCode)
    public List<Player> changePlayerReadyState(...)
    public Winner spinRoulette(...)
    // ...
}
```

**장점**:
- ✅ 불필요한 레이어 순회 제거
- ✅ Handler는 Domain Service 직접 호출 (이미 일부 Handler가 이렇게 동작)
- ✅ RoomApplicationService는 Controller 전용
- ✅ Internal 메서드 제거로 코드 간결화
- ✅ 계층 분리 명확

**단점**:
- Handler가 Domain 로직을 직접 구현 (비즈니스 로직 중복 가능성)
- Handler가 도메인 서비스 여러 개 주입 필요

---

### 옵션 2: Facade 패턴

**구조**:
```
Controller → RoomFacade
Handler → RoomFacade
Facade → RoomCommandService
Facade → RoomQueryService
```

**변경 사항**:

#### A. RoomService → RoomFacade 리네이밍
```java
@Service
public class RoomFacade {
    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;
    private final MenuCommandService menuCommandService;
    private final RoomEventPublisher roomEventPublisher;

    // Controller + Handler 공용
    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() == PlayerType.HOST) {
            return room.getPlayers();
        }

        player.updateReadyState(isReady);
        roomCommandService.save(room);
        return room.getPlayers();
    }
}
```

#### B. Internal 제거, Public으로 통일
```java
// Before
changePlayerReadyState()          // Controller용
changePlayerReadyStateInternal()  // Handler용

// After
changePlayerReadyState()          // 공용
```

#### C. Handler는 Facade 호출
```java
@Component
public class PlayerReadyEventHandler {
    private final RoomFacade roomFacade;

    public void handle(PlayerReadyEvent event) {
        final List<Player> players = roomFacade.changePlayerReadyState(...);
    }
}
```

**장점**:
- ✅ Controller와 Handler가 동일한 인터페이스 사용
- ✅ 비즈니스 로직 중복 방지 (Facade에서 통일)
- ✅ Internal 메서드 제거

**단점**:
- ❌ 여전히 Handler → Facade → Domain Service 순회
- ❌ Facade가 비대해질 가능성
- ❌ "Facade"라는 이름이 책임을 명확히 표현하지 못함

---

### 옵션 3: CQRS 분리

**구조**:
```
Controller → RoomCommandFacade → Event 발행
Controller → RoomQueryFacade → RoomQueryService

Handler → RoomCommandService (직접)
Handler → RoomQueryService (직접)
```

**변경 사항**:

#### A. Command와 Query 완전 분리
```java
@Service
public class RoomCommandFacade {
    public Room createRoom(...)
    public CompletableFuture<Room> enterRoomAsync(...)
    public boolean kickPlayer(...)
}

@Service
public class RoomQueryFacade {
    public Room getRoomByJoinCode(...)
    public boolean roomExists(...)
    public List<Player> getAllPlayers(...)
}
```

#### B. Handler는 Domain Service 직접 호출
- 옵션 1과 동일

**장점**:
- ✅ CQRS 패턴 적용으로 명령/조회 책임 분리
- ✅ 조회 성능 최적화 가능

**단점**:
- ❌ 오버엔지니어링 가능성 (현재 프로젝트에는 과도)
- ❌ 파일 수 증가

---

### 옵션 4: Application Service + Domain Service 명확화 (중간 절충안)

**구조**:
```
Controller → RoomApplicationService (오케스트레이션)
Handler → RoomDomainFacade (비즈니스 로직 캡슐화)
Domain Facade → RoomCommandService, RoomQueryService
```

**변경 사항**:

#### A. RoomDomainFacade 신규 생성
```java
@Service
public class RoomDomainFacade {
    private final RoomCommandService roomCommandService;
    private final RoomQueryService roomQueryService;

    // Handler 전용 비즈니스 로직
    public List<Player> changePlayerReadyState(String joinCode, String playerName, Boolean isReady) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        final Player player = room.findPlayer(new PlayerName(playerName));

        if (player.getPlayerType() == PlayerType.HOST) {
            return room.getPlayers();
        }

        player.updateReadyState(isReady);
        roomCommandService.save(room);
        return room.getPlayers();
    }

    public List<MiniGameType> updateMiniGames(String joinCode, String hostName, List<MiniGameType> miniGameTypes) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        room.clearMiniGames();

        miniGameTypes.forEach(miniGameType -> {
            final Playable miniGame = miniGameType.createMiniGame(joinCode);
            room.addMiniGame(new PlayerName(hostName), miniGame);
        });

        roomCommandService.save(room);
        return room.getAllMiniGame().stream().map(Playable::getMiniGameType).toList();
    }
}
```

#### B. RoomApplicationService는 Controller 전용
```java
@Service
public class RoomApplicationService {
    private final RoomDomainFacade roomDomainFacade;
    private final RoomEventPublisher roomEventPublisher;

    // Controller 전용 (복잡한 오케스트레이션)
    @Transactional
    public Room createRoom(String hostName, SelectedMenuRequest selectedMenuRequest) {
        // 복잡한 흐름 제어
        // Event 발행
        // 비동기 처리
    }
}
```

#### C. Handler는 DomainFacade 호출
```java
@Component
public class PlayerReadyEventHandler {
    private final RoomDomainFacade roomDomainFacade;

    public void handle(PlayerReadyEvent event) {
        final List<Player> players = roomDomainFacade.changePlayerReadyState(...);
    }
}
```

**장점**:
- ✅ 비즈니스 로직 재사용 (Handler 간 중복 방지)
- ✅ Application Service와 Domain Facade 역할 명확 분리
- ✅ Controller와 Handler의 계층 구분 명확

**단점**:
- ❌ 파일 1개 추가 (RoomDomainFacade)
- ❌ 여전히 Handler → Facade → Domain Service 순회

---

## 4. 권장 방안

### ⭐ 옵션 1 추천: Handler가 Domain Service 직접 호출

**이유**:

1. **이미 일부 Handler가 이 방식 사용**
   - `RoomCreateEventHandler`: RoomCommandService 직접 호출
   - `RoomJoinStreamHandler`: RoomCommandService 직접 호출
   - `RouletteSpinEventHandler`: RoulettePersistenceService 직접 호출

2. **불필요한 레이어 제거**
   - Handler → RoomService → Domain Service (3단계)
   - Handler → Domain Service (2단계)

3. **책임 분리 명확**
   - RoomApplicationService: Controller 전용 (복잡한 오케스트레이션)
   - Handler: Domain Service 직접 호출 (단순 비즈니스 로직)

4. **코드 간결화**
   - Internal 메서드 모두 제거
   - RoomApplicationService 파일 크기 감소 (323줄 → 약 200줄)

5. **일관성 확보**
   - 모든 Handler가 동일한 패턴 사용

**구현 계획**:

### Phase 1: RoomApplicationService 정리
1. RoomService → RoomApplicationService 리네이밍
2. RoomJpaRepository 의존성 제거
3. saveRoomEntity() → RoomPersistenceService로 이동

### Phase 2: Internal 메서드 제거
1. changePlayerReadyStateInternal() 삭제
2. getPlayersInternal() 삭제
3. updateMiniGamesInternal() 삭제

### Phase 3: Handler 리팩토링
1. PlayerReadyEventHandler → RoomCommandService 직접 호출
2. PlayerKickEventHandler → RoomCommandService 직접 호출
3. PlayerListUpdateEventHandler → RoomQueryService 직접 호출
4. MiniGameSelectEventHandler → RoomCommandService 직접 호출
5. RouletteShowEventHandler → RoomCommandService 직접 호출

### Phase 4: 영속성 계층 정리
1. RoomPersistenceService 생성 (Infrastructure Layer)
2. RouletteService, RoulettePersistenceService 일관성 확보

---

## 5. 예상 효과

### Before vs After

#### Before: 복잡한 호출 구조
```
Controller
  └─→ RoomService.createRoom()
        └─→ Event 발행
              └─→ PlayerReadyEventHandler
                    └─→ RoomService.changePlayerReadyStateInternal()  ⚠️
                          └─→ RoomCommandService.save()
```

#### After: 단순한 호출 구조
```
Controller
  └─→ RoomApplicationService.createRoom()
        └─→ Event 발행
              └─→ PlayerReadyEventHandler
                    └─→ RoomCommandService.save()  ✅
```

### 코드 감소
- RoomService: 323줄 → 약 200줄 (38% 감소)
- Internal 메서드 3개 삭제 (약 50줄)

### 계층 분리 명확화
```
Presentation Layer
  └─→ Application Layer (RoomApplicationService)
        └─→ Domain Layer (RoomCommandService, RoomQueryService)
              └─→ Infrastructure Layer (RoomRepository, RoomPersistenceService)

Event Handlers (Infrastructure Layer)
  └─→ Domain Layer (직접 호출)
```

### 유지보수성 향상
- Handler별 책임 명확
- RoomApplicationService는 Controller 전용
- Internal 메서드 제거로 혼란 감소

---

## 6. 다음 단계

진행할까요?
1. ⭐ 옵션 1: Handler가 Domain Service 직접 호출
2. 옵션 2: Facade 패턴
3. 옵션 4: DomainFacade 추가

어떤 방식으로 진행하시겠습니까?
