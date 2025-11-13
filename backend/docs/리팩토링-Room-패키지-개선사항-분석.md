# Room 패키지 개선사항 분석

## 1. 개요

Room 패키지(120개 파일)를 전체적으로 분석하여 계층별 의존성 문제와 책임 혼재 문제를 파악하고 개선 방안을 제시합니다.

---

## 2. 발견된 문제점

### 2.1 계층별 의존성 문제 (Layer Violation)

#### 🔴 심각: Application Layer → Infrastructure Layer 의존성

**위치**: `RoomService.java:28-29`
```java
import coffeeshout.room.infra.messaging.RoomEventWaitManager;
import coffeeshout.room.infra.persistence.RoomPersistenceService;
```

**문제점**:
- Application 계층이 Infrastructure 계층의 구체 클래스에 직접 의존
- DDD 계층 원칙 위배 (Application은 Domain에만 의존해야 함)
- 테스트 시 Infrastructure 계층 Mock 필요

**영향 범위**:
- `RoomEventWaitManager`: `enterRoomAsync()` 등 비동기 메서드에서 사용 (3곳)
- `RoomPersistenceService`: `createRoom()`, `saveRoomSession()` 등에서 사용 (1곳)

**Handler의 Infrastructure 의존**:
- `RouletteShowEventHandler` → `RoomPersistenceService`
- `RouletteSpinEventHandler` → `RoomPersistenceService`

> **참고**: Handler는 Infrastructure 계층 접근이 허용될 수 있으나, 일관성을 위해 검토 필요

---

### 2.2 책임 혼재 문제 (Mixed Responsibilities)

#### 🔴 심각: RoomService의 과도한 책임

**현재 구조**:
```
RoomService (295 lines)
├── 방 생성/참가 (createRoom, enterRoom, enterRoomAsync)
├── 플레이어 관리 (changePlayerReadyState, getAllPlayers, removePlayer, kickPlayer)
├── 미니게임 관리 (updateMiniGames, getAllMiniGames, getSelectedMiniGames, getMiniGameScores, getMiniGameRanks)
├── 메뉴 선택 (selectMenu)
├── 룰렛 (spinRoulette, showRoulette, getProbabilities)
├── QR 코드 (getQrCodeStatus)
└── 유틸리티 (roomExists, isGuestNameDuplicated, isReadyState, hasPlayer)
```

**문제점**:
1. **단일 책임 원칙(SRP) 위배**: 한 클래스가 너무 많은 관심사를 처리
2. **응집도 저하**: 관련 없는 기능들이 한 곳에 모여 있음
3. **유지보수 어려움**: 변경 시 영향 범위가 넓음

**통계**:
- 총 메서드 수: ~25개
- `getByJoinCode` 호출: 17회 (중복 패턴)
- `roomCommandService.save` 호출: 3회

---

#### 🟡 중간: Thin Wrapper 서비스

**1. MenuService** (22 lines)
```java
@Service
public class MenuService {
    private final MenuQueryService menuQueryService;

    public List<ProvidedMenu> getAll() {
        return menuQueryService.getAll();  // 단순 위임
    }

    public List<ProvidedMenu> getAllMenuByCategoryId(Long categoryId) {
        return menuQueryService.getAllByCategoryId(categoryId);  // 단순 위임
    }
}
```

**2. MenuCategoryService** (18 lines)
```java
@Service
public class MenuCategoryService {
    private final MenuCategoryQueryService menuCategoryQueryService;

    public List<MenuCategory> getAll() {
        return menuCategoryQueryService.getAll();  // 단순 위임
    }
}
```

**문제점**:
- Application 계층에서 Domain Service를 단순 위임만 하는 래퍼
- 추가 로직이 없어 불필요한 계층 추가
- Controller가 Domain Service를 직접 호출하는 것과 차이 없음

**영향**:
- 코드 복잡도 증가
- 불필요한 메서드 호출 체인

---

#### 🟢 정보: @Repository가 Domain 계층에 존재

**위치**:
- `MemoryRoomRepository.java` (domain/repository)
- `MemoryMenuRepository.java` (domain/repository)
- `MemoryMenuCategoryRepository.java` (domain/repository)

**현재 구조**:
```
domain/
└── repository/
    ├── RoomRepository (interface)
    └── MemoryRoomRepository (@Repository, implements RoomRepository)
```

**분석**:
- Spring의 `@Repository`는 Infrastructure 계층 관심사
- 하지만 In-Memory 구현체는 DDD에서 종종 Domain 계층에 위치
- 현재는 큰 문제 없으나, 향후 Redis/DB 구현 시 Infrastructure로 이동 필요

**권장사항**:
- 현재는 유지 (In-Memory 특성상 Domain과 밀접)
- Write-back cache 구현 시 구조 재검토

---

### 2.3 중복 패턴 (Duplication)

#### Room 조회 → 변경 → 저장 패턴

**통계**:
- `roomQueryService.getByJoinCode()`: 22회 호출 (6개 파일)
- `roomCommandService.save()`: 6회 호출 (4개 파일)

**예시 (PlayerReadyEventHandler)**:
```java
final Room room = roomQueryService.getByJoinCode(new JoinCode(event.joinCode()));
final Player player = room.findPlayer(new PlayerName(event.playerName()));
player.updateReadyState(event.isReady());
roomCommandService.save(room);
```

**문제점**:
- 동일한 패턴이 여러 곳에 반복
- `JoinCode`, `PlayerName` 객체 생성 반복
- 보일러플레이트 코드 증가

**참고**:
- 이는 CQRS 패턴의 자연스러운 결과이므로 심각한 문제는 아님
- 다만, 공통 유틸리티 메서드로 추출 가능

---

## 3. 개선 방안

### 3.1 계층 의존성 해결

#### 옵션 1: Interface 도입 (추천)

**적용 대상**: RoomEventWaitManager, RoomPersistenceService

**구조**:
```
application/
└── port/
    ├── RoomEventWaitPort (interface)
    └── RoomPersistencePort (interface)

infra/
├── messaging/
│   └── RoomEventWaitManager (implements RoomEventWaitPort)
└── persistence/
    └── RoomPersistenceService (implements RoomPersistencePort)
```

**장점**:
- Application 계층이 Infrastructure 세부사항에 의존하지 않음
- 테스트 용이성 향상 (Interface Mock)
- Hexagonal Architecture / Clean Architecture 준수

**단점**:
- Interface 추가로 파일 수 증가
- 간단한 기능에는 과도할 수 있음

---

#### 옵션 2: Handler 책임 재분배

**적용 대상**: RouletteShowEventHandler, RouletteSpinEventHandler

**현재**:
```java
public class RouletteSpinEventHandler {
    private final RoomPersistenceService roomPersistenceService;  // infra 의존

    public void handle(RouletteSpinEvent event) {
        // ...
        roomPersistenceService.saveRouletteResult(event);
    }
}
```

**개선안**:
```java
public class RouletteSpinEventHandler {
    private final RouletteDomainService rouletteDomainService;  // domain 의존

    public void handle(RouletteSpinEvent event) {
        // ...
        rouletteDomainService.processRouletteResult(event);  // domain이 persistence 호출
    }
}
```

**장점**:
- Handler가 Domain 계층만 의존
- Persistence 로직을 Domain Service로 캡슐화

**단점**:
- Domain Service가 Infrastructure에 의존하게 됨 (새로운 문제)

---

#### 옵션 3: 현상 유지 (Handler만 예외 허용)

**판단 기준**:
- RoomService의 Infrastructure 의존은 해결 필요 (Application Layer의 핵심)
- Handler의 Infrastructure 의존은 허용 가능 (이벤트 처리 특성상)

**적용**:
- RoomService → RoomEventWaitManager, RoomPersistenceService 의존 제거
- Handler → RoomPersistenceService 의존은 유지

---

### 3.2 RoomService 책임 분리

#### 옵션 1: 관심사별 Service 분리 (추천)

**분리 방안**:
```
RoomService (방 생성/참가/삭제)
├── createRoom()
├── enterRoom()
├── enterRoomAsync()
├── removePlayer()
├── kickPlayer()
└── roomExists()

RoomPlayerService (플레이어 관리)
├── changePlayerReadyState()
├── getAllPlayers()
├── isGuestNameDuplicated()
└── isReadyState()

RoomMiniGameService (미니게임 관리)
├── updateMiniGames()
├── getAllMiniGames()
├── getSelectedMiniGames()
├── getMiniGameScores()
└── getMiniGameRanks()

RoomRouletteService (룰렛)
├── spinRoulette()
├── showRoulette()
└── getProbabilities()

(QR, Menu는 이미 별도 Service 존재)
```

**장점**:
- 단일 책임 원칙 준수
- 각 Service의 크기와 복잡도 감소
- 변경 영향 범위 축소

**단점**:
- Service 파일 수 증가 (1개 → 4개)
- Service 간 의존성 관리 필요

---

#### 옵션 2: Facade 패턴 (보류)

**구조**:
```
RoomFacade (Controller 진입점)
└── calls → RoomService, PlayerService, MiniGameService, RouletteService
```

**판단**:
- 현재 구조에서는 과도한 복잡도 증가
- 옵션 1 적용 후 필요 시 재검토

---

### 3.3 Thin Wrapper 제거

#### MenuService, MenuCategoryService 제거

**변경 전**:
```
Controller → MenuService → MenuQueryService
```

**변경 후**:
```
Controller → MenuQueryService (직접 호출)
```

**적용 방법**:
1. Controller에서 MenuQueryService 직접 주입
2. MenuService, MenuCategoryService 삭제
3. 기존 호출부 수정

**장점**:
- 불필요한 계층 제거
- 메서드 호출 체인 축소

**단점**:
- Controller가 Domain 계층 직접 의존 (DDD 관점에서 논쟁 여지)

---

### 3.4 중복 패턴 개선

#### 공통 유틸리티 추출

**현재 반복 패턴**:
```java
final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
// ... 변경 로직 ...
roomCommandService.save(room);
```

**개선안 1: Template Method**:
```java
public abstract class RoomUpdateTemplate {
    protected void updateRoom(String joinCode, Consumer<Room> updateLogic) {
        final Room room = roomQueryService.getByJoinCode(new JoinCode(joinCode));
        updateLogic.accept(room);
        roomCommandService.save(room);
    }
}
```

**개선안 2: 현상 유지**:
- CQRS 패턴의 자연스러운 결과
- 과도한 추상화보다 명시적 코드가 나을 수 있음

**권장**: **현상 유지** (가독성 우선)

---

## 4. 우선순위 및 적용 순서

### Phase 1: 긴급 (Write-back Cache 구현 전 필수)

1. **RoomService 책임 분리** (옵션 1)
   - 영향도: 높음
   - 난이도: 중간
   - 예상 시간: 2-3시간

2. **Thin Wrapper 제거** (MenuService, MenuCategoryService)
   - 영향도: 낮음
   - 난이도: 낮음
   - 예상 시간: 30분

### Phase 2: 중요 (구조 개선)

3. **RoomService Infrastructure 의존성 제거** (옵션 1: Interface 도입)
   - 영향도: 중간
   - 난이도: 중간
   - 예상 시간: 1-2시간

### Phase 3: 선택 (Write-back Cache 구현 후)

4. **@Repository 위치 재검토**
   - Write-back cache 구현 시 Repository 구조 변경 필요
   - 그때 함께 검토

---

## 5. 결론

**현재 상태**:
- 전반적으로 잘 구조화되어 있으나, RoomService의 과도한 책임이 주요 문제
- 계층 의존성은 일부 위배되나 큰 장애는 아님

**권장 순서**:
1. RoomService 책임 분리 (필수)
2. Thin Wrapper 제거 (빠르고 쉬운 개선)
3. Infrastructure 의존성 해결 (구조 개선)

**다음 단계**:
- 위 개선사항 적용 후 → Write-back Cache 구현 진행
