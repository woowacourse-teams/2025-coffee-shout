# 메모리 저장소와 DB 저장소 혼재 분석 및 개선 방안

## 1. 현재 구조 분석

### 1.1 이중 저장소 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    실시간 게임 (메모리)                        │
│  MemoryRoomRepository (ConcurrentHashMap)                   │
│  - Room 도메인 객체                                          │
│  - 게임 진행 중 모든 상태 변경                                │
│  - 빠른 조회/저장 (실시간성 보장)                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 특정 이벤트 시점에만
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   통계/기록 (Database)                        │
│  RoomJpaRepository, PlayerJpaRepository, etc.               │
│  - RoomEntity (room_session 테이블)                         │
│  - PlayerEntity (player 테이블)                             │
│  - RouletteResultEntity (roulette_result 테이블)            │
│  - MiniGameEntity, MiniGameResultEntity                     │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 메모리 저장소 (Primary)

**MemoryRoomRepository.java**
```java
@Repository
public class MemoryRoomRepository implements RoomRepository {
    private final Map<JoinCode, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public Room save(Room room) {
        rooms.put(room.getJoinCode(), room);
        return rooms.get(room.getJoinCode());
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        return Optional.ofNullable(rooms.get(joinCode));
    }
}
```

**특징**:
- ConcurrentHashMap 사용 (Thread-safe)
- Room 도메인 객체를 직접 저장
- 인메모리 저장 → 빠른 조회/저장
- 서버 재시작 시 **데이터 손실**

**사용처**:
- `RoomCommandService.save()` (46회 사용)
- `RoomQueryService.getByJoinCode()` (46회 사용)
- 게임 진행 중 모든 Room 조회/저장

### 1.3 DB 저장소 (Secondary)

**Entity 구조**:

#### RoomEntity (room_session 테이블)
```java
@Entity
@Table(name = "room_session")
public class RoomEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 5)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    private RoomState roomStatus;  // READY, ROULETTE, PLAYING, DONE

    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
}
```

#### PlayerEntity (player 테이블)
```java
@Entity
@Table(name = "player")
public class PlayerEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private RoomEntity roomSession;

    @Column(nullable = false)
    private String playerName;

    @Enumerated(EnumType.STRING)
    private PlayerType playerType;  // HOST, GUEST

    private LocalDateTime createdAt;
}
```

#### RouletteResultEntity (roulette_result 테이블)
```java
@Entity
@Table(name = "roulette_result")
public class RouletteResultEntity {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private RoomEntity roomSession;

    @ManyToOne(fetch = FetchType.LAZY)
    private PlayerEntity winner;

    private Integer winnerProbability;
    private LocalDateTime createdAt;
}
```

**사용처**:
- `RoomJpaRepository` (33회 사용)
- 특정 이벤트 시점에만 DB 저장

---

## 2. 저장 시점 분석

### 2.1 메모리 저장 (실시간)

| 시점 | 메서드 | 저장 내용 |
|------|--------|----------|
| 방 생성 | `RoomCommandService.saveIfAbsentRoom()` | Room 객체 생성 및 저장 |
| 게스트 입장 | `RoomCommandService.joinGuest()` | Room에 Player 추가 |
| Ready 상태 변경 | `PlayerReadyEventHandler` | Player의 ready 상태 변경 |
| 미니게임 선택 | `MiniGameSelectEventHandler` | Room의 miniGames 리스트 업데이트 |
| 룰렛 전환 | `RouletteShowEventHandler` | Room의 상태 변경 (ROULETTE) |
| 룰렛 스핀 | `RoomService.spinRoulette()` | Winner 결정 |
| 플레이어 강퇴 | `PlayerKickEventHandler` | Room에서 Player 제거 |
| 카드 게임 | `CardGameCommandService` | 카드 선택 등 |

**총 조회 횟수**: 46회 (`roomQueryService.getByJoinCode`)

### 2.2 DB 저장 (특정 시점)

| 시점 | 클래스 | 저장 내용 | 비고 |
|------|--------|----------|------|
| **방 생성** | `RoomPersistenceService` | RoomEntity 생성 (READY 상태) | ✅ 방 생성 직후 |
| **게임 시작** | `MiniGamePersistenceService` | PlayerEntity 생성 (첫 미니게임 시작 시) | ⚠️ 방 입장 시가 아님 |
| | | RoomEntity 상태 업데이트 (PLAYING) | |
| | | MiniGameEntity 생성 | |
| **룰렛 전환** | `RouletteService` | RoomEntity 상태 업데이트 (ROULETTE) | @RedisLock으로 보호 |
| **룰렛 결과** | `RouletteService` | RoomEntity 상태 업데이트 (DONE) | |
| | | RoomEntity.finishedAt 설정 | |
| | | RouletteResultEntity 생성 | |
| **미니게임 결과** | `MiniGameResultSaveEventListener` | MiniGameResultEntity 생성 | Spring Event 사용 |

**총 사용 횟수**: 33회 (`roomJpaRepository`)

---

## 3. 핵심 문제점

### 3.1 데이터 동기화 문제 ⚠️

#### 문제 1: PlayerEntity가 방 입장 시 저장되지 않음

```java
// 방 입장 (메모리만 저장)
RoomCommandService.joinGuest()
  └─→ room.joinGuest(playerName, selectedMenu)
  └─→ roomRepository.save(room)  // 메모리에만 저장

// DB 저장은 첫 미니게임 시작 시에만 발생
MiniGamePersistenceService.saveGameEntities()
  └─→ if (room.isFirstStarted()) {
        room.getPlayers().forEach(player -> {
            playerJpaRepository.save(new PlayerEntity(...));  // 이 시점에 저장
        });
      }
```

**문제점**:
- 방에 입장한 플레이어가 게임을 시작하기 전에는 DB에 기록되지 않음
- 통계나 대시보드에서 "방 입장 기록"을 조회할 수 없음
- 서버 재시작 시 게임 시작 전 플레이어 데이터 손실

#### 문제 2: Room 상태 불일치

```java
// 메모리의 Room
Room {
    joinCode: "ABC12"
    players: [Host, Guest1, Guest2, Guest3]  // 4명
    roomState: READY
    miniGames: [CARD_GAME, RACING_GAME]
}

// DB의 RoomEntity
RoomEntity {
    joinCode: "ABC12"
    roomStatus: READY  // ✅ 일치
}

// DB의 PlayerEntity
PlayerEntity 테이블 → 0개 (게임 시작 전이므로 저장 안 됨)  // ⚠️ 불일치
```

**문제점**:
- 메모리에는 4명의 플레이어가 있지만, DB에는 0명
- Dashboard에서 "현재 진행 중인 방" 조회 시 플레이어 정보 없음
- 룰렛 결과 저장 시 PlayerEntity가 없어서 FK 참조 불가능 (게임 시작 전 룰렛 불가능하지만, 논리적 불일치)

#### 문제 3: RoomEntity.roomStatus와 Room.roomState 동기화

```java
// 메모리 업데이트
room.showRoulette();  // Room.roomState = ROULETTE
roomCommandService.save(room);  // 메모리에만 반영

// DB 업데이트 (별도 호출)
roulettePersistenceService.saveRoomStatus(event);
  └─→ rouletteService.updateRoomStatusToRoulette(joinCode);
      └─→ roomEntity.updateRoomStatus(RoomState.ROULETTE);  // DB에 반영
```

**문제점**:
- 메모리 업데이트와 DB 업데이트가 분리되어 있음
- 한쪽만 실패하면 데이터 불일치 발생
- @RedisLock으로 중복 저장은 방지하지만, 동기화는 보장하지 않음

### 3.2 서버 재시작 시 데이터 손실 🔥

```
사용자 A: 방 생성 (ABC12)
사용자 B: 방 입장 (ABC12)
사용자 C: 방 입장 (ABC12)
[메모리: Room {players: 3명}, DB: RoomEntity만 존재, PlayerEntity 없음]

↓ 서버 재시작 ↓

[메모리: 전부 사라짐 💥, DB: RoomEntity만 존재]

사용자 D: 방 입장 시도 (ABC12) → 방이 존재하지 않음 (메모리에 없음)
```

**문제점**:
- 게임 진행 중 서버 재시작 시 모든 게임 세션 손실
- DB에 RoomEntity는 있지만, 메모리에 Room이 없어서 조회 불가
- 사용자는 게임을 계속 할 수 없음

### 3.3 트랜잭션 일관성 부재 ⚠️

```java
@Transactional
public void someMethod() {
    // 메모리 저장 (트랜잭션 관리 안 됨)
    roomCommandService.save(room);

    // DB 저장 (트랜잭션 관리됨)
    roomPersistenceService.saveRoomSession(joinCode);

    // 만약 DB 저장 실패 시?
    // → 메모리에는 저장됨, DB에는 저장 안 됨
}
```

**문제점**:
- 메모리 저장은 트랜잭션 롤백 불가
- DB 저장 실패 시에도 메모리에는 반영됨
- 데이터 일관성 보장 불가

### 3.4 복잡도 증가 📈

**저장소 2개 관리**:
- MemoryRoomRepository (도메인 계층)
- RoomJpaRepository (인프라 계층)
- RoomPersistenceService (인프라 계층)
- RouletteService (애플리케이션 계층)
- MiniGamePersistenceService (애플리케이션 계층)

**각각 다른 저장 시점**:
- 방 생성: 메모리 + DB (RoomEntity)
- 플레이어 입장: 메모리만
- 게임 시작: 메모리 + DB (PlayerEntity, MiniGameEntity)
- 룰렛 전환: 메모리 + DB (RoomEntity 상태)
- 룰렛 결과: 메모리 + DB (RouletteResultEntity)

---

## 4. 통계 분석

### 4.1 코드 사용 빈도

| 저장소 | 메서드 | 사용 횟수 | 비율 |
|--------|--------|----------|------|
| **메모리** | `roomQueryService.getByJoinCode()` | 46회 | 58% |
| **DB** | `roomJpaRepository.*` | 33회 | 42% |
| **합계** | | 79회 | 100% |

### 4.2 Entity별 저장 시점

| Entity | 저장 시점 | 필요 여부 | 비고 |
|--------|----------|----------|------|
| RoomEntity | 방 생성 시 | ✅ 필수 | 방 세션 기록 |
| PlayerEntity | 첫 미니게임 시작 시 | ⚠️ 늦음 | 방 입장 시 저장해야 함 |
| MiniGameEntity | 미니게임 시작 시 | ✅ 적절 | |
| RouletteResultEntity | 룰렛 스핀 시 | ✅ 적절 | |
| MiniGameResultEntity | 미니게임 종료 시 | ✅ 적절 | |

---

## 5. 개선 방안

### 옵션 1: Write-Through Cache ⭐ 추천

**개념**:
- 메모리는 Cache 역할
- 모든 쓰기 작업 시 DB에도 즉시 반영
- 읽기는 메모리에서만 (빠른 조회)

**구조**:
```
┌──────────────────────────────────────┐
│    RoomCommandService.save()         │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  RoomRepository (인터페이스)          │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  CachedRoomRepository (구현체)        │
│  - Memory: 빠른 조회                  │
│  - DB: 영속화 (Write-Through)        │
└──────────────────────────────────────┘
```

**구현**:
```java
@Repository
@RequiredArgsConstructor
public class CachedRoomRepository implements RoomRepository {

    private final Map<JoinCode, Room> cache = new ConcurrentHashMap<>();
    private final RoomJpaRepository roomJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;

    @Override
    @Transactional
    public Room save(Room room) {
        // 1. 메모리 캐시에 저장
        cache.put(room.getJoinCode(), room);

        // 2. DB에도 즉시 저장 (Write-Through)
        syncToDatabase(room);

        return room;
    }

    private void syncToDatabase(Room room) {
        // RoomEntity 동기화
        RoomEntity roomEntity = findOrCreateRoomEntity(room.getJoinCode());
        roomEntity.updateRoomStatus(room.getRoomState());
        roomJpaRepository.save(roomEntity);

        // PlayerEntity 동기화 (방 입장 시점에 바로 저장)
        room.getPlayers().forEach(player -> {
            if (!playerExists(roomEntity, player.getName().value())) {
                PlayerEntity playerEntity = new PlayerEntity(
                    roomEntity,
                    player.getName().value(),
                    player.getPlayerType()
                );
                playerJpaRepository.save(playerEntity);
            }
        });
    }

    @Override
    public Optional<Room> findByJoinCode(JoinCode joinCode) {
        // 메모리에서만 조회 (빠름)
        return Optional.ofNullable(cache.get(joinCode));
    }
}
```

**장점**:
- ✅ 데이터 일관성 보장 (메모리 = DB)
- ✅ 플레이어 입장 즉시 DB 저장
- ✅ 트랜잭션 관리 용이
- ✅ 기존 코드 변경 최소화 (RoomRepository 구현체만 교체)

**단점**:
- ❌ 쓰기 성능 저하 (DB 저장 오버헤드)
- ❌ DB 저장 실패 시 처리 필요

---

### 옵션 2: Write-Back Cache

**개념**:
- 메모리에만 즉시 저장
- 일정 주기 또는 이벤트 시점에 DB에 일괄 저장
- 성능 우선

**구조**:
```
┌──────────────────────────────────────┐
│  메모리에 즉시 저장                    │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  Dirty Marking (변경 추적)            │
└──────────────┬───────────────────────┘
               │
               ▼ 주기적 or 이벤트 시점
┌──────────────────────────────────────┐
│  DB에 일괄 저장                       │
└──────────────────────────────────────┘
```

**구현**:
```java
@Repository
public class WriteBackCachedRoomRepository implements RoomRepository {

    private final Map<JoinCode, Room> cache = new ConcurrentHashMap<>();
    private final Set<JoinCode> dirtyRooms = ConcurrentHashMap.newKeySet();

    @Override
    public Room save(Room room) {
        cache.put(room.getJoinCode(), room);
        dirtyRooms.add(room.getJoinCode());  // Dirty 마킹
        return room;
    }

    @Scheduled(fixedRate = 5000)  // 5초마다 DB 동기화
    @Transactional
    public void flushToDatabase() {
        dirtyRooms.forEach(joinCode -> {
            Room room = cache.get(joinCode);
            if (room != null) {
                syncToDatabase(room);
            }
        });
        dirtyRooms.clear();
    }
}
```

**장점**:
- ✅ 쓰기 성능 우수 (메모리만 저장)
- ✅ DB 부하 감소 (일괄 처리)

**단점**:
- ❌ 데이터 유실 위험 (서버 다운 시)
- ❌ 데이터 일관성 보장 어려움 (일시적 불일치)
- ❌ 복잡도 증가 (Dirty Tracking, Flush 로직)

---

### 옵션 3: Event Sourcing

**개념**:
- 모든 상태 변경을 이벤트로 저장
- 이벤트를 재생하여 현재 상태 복원
- CQRS와 함께 사용

**구조**:
```
┌──────────────────────────────────────┐
│  이벤트 발생 (RoomCreated, PlayerJoined, etc.) │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  Event Store (DB)                    │
│  - 모든 이벤트 순차 저장               │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│  Event Replay → 현재 상태 (메모리)     │
└──────────────────────────────────────┘
```

**장점**:
- ✅ 완벽한 감사 로그 (Audit Log)
- ✅ 시점별 상태 복원 가능
- ✅ 서버 재시작 시 이벤트 재생으로 복원

**단점**:
- ❌ 복잡도 매우 높음
- ❌ 이벤트 스키마 관리 어려움
- ❌ 현재 프로젝트에는 과도 (Overkill)

---

### 옵션 4: 현재 구조 유지 + 개선

**개선 사항**:

#### 4-1. PlayerEntity 저장 시점 변경
```java
// Before: 첫 미니게임 시작 시
MiniGamePersistenceService.saveGameEntities()
  └─→ if (room.isFirstStarted()) { ... }

// After: 방 입장 시
RoomJoinStreamHandler.handle()
  └─→ roomCommandService.joinGuest(...)
  └─→ roomPersistenceService.savePlayer(roomEntity, player)  // 즉시 저장
```

#### 4-2. 동기화 메서드 추가
```java
@Component
public class RoomSyncService {

    @Transactional
    public void syncRoomToDatabase(Room room) {
        RoomEntity roomEntity = findOrCreateRoomEntity(room.getJoinCode());
        roomEntity.updateRoomStatus(room.getRoomState());

        // 플레이어 동기화
        room.getPlayers().forEach(player -> {
            if (!playerExists(roomEntity, player.getName().value())) {
                playerJpaRepository.save(new PlayerEntity(roomEntity, ...));
            }
        });
    }
}
```

#### 4-3. 서버 재시작 시 복구 로직
```java
@Component
@RequiredArgsConstructor
public class RoomRecoveryService {

    @PostConstruct
    public void recoverRoomsFromDatabase() {
        // DB에서 READY, ROULETTE, PLAYING 상태의 방 조회
        List<RoomEntity> activeRooms = roomJpaRepository.findByRoomStatusIn(
            List.of(RoomState.READY, RoomState.ROULETTE, RoomState.PLAYING)
        );

        // 메모리로 복원 (단, 완전한 복원은 어려움)
        activeRooms.forEach(roomEntity -> {
            // 제한적 복원만 가능 (플레이어 목록, 게임 상태 등은 복원 어려움)
            log.warn("방 복구 실패 가능: joinCode={}", roomEntity.getJoinCode());
        });
    }
}
```

**장점**:
- ✅ 최소한의 변경
- ✅ 일부 문제만 해결

**단점**:
- ❌ 근본적인 동기화 문제 미해결
- ❌ 서버 재시작 시 완전 복구 불가능

---

## 6. 권장 방안

### ⭐ 옵션 1: Write-Through Cache 추천

**이유**:

1. **데이터 일관성 보장**
   - 메모리와 DB가 항상 동기화
   - 트랜잭션 관리 용이

2. **실시간성 유지**
   - 읽기는 메모리에서만 (빠름)
   - 쓰기만 DB 추가 (허용 가능한 오버헤드)

3. **통계/분석 가능**
   - PlayerEntity가 방 입장 시 저장
   - Dashboard에서 실시간 조회 가능

4. **서버 재시작 대응**
   - DB에 모든 데이터 저장
   - 복구 로직 구현 가능 (제한적이지만)

5. **기존 코드 변경 최소화**
   - RoomRepository 구현체만 교체
   - Domain Service 로직 변경 불필요

**구현 계획**:

### Phase 1: CachedRoomRepository 구현
1. MemoryRoomRepository를 CachedRoomRepository로 변경
2. save() 메서드에 DB 동기화 로직 추가
3. syncToDatabase() 메서드 구현

### Phase 2: PlayerEntity 즉시 저장
1. RoomJoinStreamHandler에서 PlayerEntity 저장
2. RoomPersistenceService에 savePlayer() 메서드 추가

### Phase 3: 기존 PersistenceService 정리
1. RouletteService의 RoomEntity 상태 업데이트 제거 (CachedRoomRepository에서 자동 처리)
2. MiniGamePersistenceService의 PlayerEntity 저장 로직 제거 (이미 저장됨)

### Phase 4: 테스트 및 검증
1. 데이터 동기화 확인
2. 성능 테스트 (DB 저장 오버헤드 측정)
3. 트랜잭션 일관성 검증

---

## 7. 대안: Redis를 캐시로 사용

현재 메모리 대신 Redis를 사용하는 방안도 고려 가능:

**장점**:
- ✅ 서버 재시작 시에도 데이터 유지
- ✅ 멀티 인스턴스 환경에서 데이터 공유 가능
- ✅ TTL 설정으로 자동 만료 가능

**단점**:
- ❌ Redis 장애 시 서비스 불가
- ❌ 네트워크 오버헤드
- ❌ Redis 추가 관리 필요

**현재는 Redis Pub/Sub만 사용 중이므로, 추후 고려 가능**

---

## 8. 결론

**현재 문제**:
- 메모리와 DB가 분리되어 데이터 일관성 문제
- PlayerEntity가 게임 시작 시에만 저장 (방 입장 시 저장 안 됨)
- 서버 재시작 시 게임 세션 전부 손실
- 트랜잭션 일관성 보장 어려움

**권장 개선 방안**:
- ⭐ **Write-Through Cache 패턴 적용**
- CachedRoomRepository 구현
- 모든 쓰기 작업 시 메모리 + DB 동시 저장
- 읽기는 메모리에서만 (성능 유지)

**예상 효과**:
- ✅ 데이터 일관성 보장
- ✅ 통계/분석 가능 (실시간 PlayerEntity 저장)
- ✅ 트랜잭션 관리 용이
- ✅ 기존 코드 변경 최소화

진행할까요?
