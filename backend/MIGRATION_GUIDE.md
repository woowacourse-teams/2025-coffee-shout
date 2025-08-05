# 🚀 카드게임 라운드 관리 시스템 마이그레이션 가이드

## 📋 변경 사항 요약

기존의 복잡한 Task 시스템을 제거하고, 방별 독립적인 라운드 관리 시스템으로 완전히 교체했습니다.

## ❌ 제거된 기존 시스템

### 1. **제거된 클래스들**
- `CardGameTaskExecutorsV2` → `RoundManagerRegistry`로 대체
- `MiniGameTaskManager` → `RoomRoundManager`로 대체  
- `ChainedTask` → `RoundPhaseHandler`로 대체
- `CardGameTaskType` → `RoundPhase`로 대체

### 2. **제거된 Enum들**
- `CardGameState` → `RoundPhase`로 대체
- `CardGameRound` → `RoundState`로 대체

## ✅ 새로운 시스템

### 1. **핵심 클래스들**
```java
// 방별 라운드 관리 레지스트리
RoundManagerRegistry registry;

// 각 방의 전용 라운드 매니저
RoomRoundManager manager = registry.getOrCreate(joinCode);

// 통합된 라운드 상태
RoundState roundState = new RoundState(roundNumber, phase);

// 단계별 처리 핸들러
RoundPhaseHandler handler;
```

### 2. **마이그레이션된 메서드들**
```java
// Before (제거됨)
cardGameTaskExecutors.put(joinCode, manager);
cardGameTaskExecutors.get(joinCode).startWith(FIRST_ROUND_LOADING);

// After (새 방식)
RoomRoundManager manager = roundManagerRegistry.getOrCreate(joinCode);
manager.executePhase(cardGame, room, onStateChange);
```

### 3. **새로운 설정**
```yaml
# application.yml
card-game:
  max-rounds: 2
  phases:
    loading:
      duration: PT3S
    playing:
      duration: PT10S
      early-skip: true
    scoring:
      duration: PT1.5S
```

## 🔧 코드 업데이트 가이드

### CardGame 클래스 사용법
```java
// Before (제거됨)
cardGame.getState() // CardGameState enum
cardGame.getRound() // CardGameRound enum

// After (새 방식)
cardGame.getRoundState() // RoundState 객체
cardGame.getCurrentPhase() // RoundPhase enum
cardGame.getCurrentRoundNumber() // int
```

### 카드 선택 검증
```java
// Before (제거됨)  
state == CardGameState.PLAYING

// After (새 방식)
roundState.isPlayingPhase()
```

### 라운드 완료 체크
```java
// Before (제거됨)
cardGame.isFinished(CardGameRound.FIRST)

// After (새 방식)  
cardGame.allPlayersSelected()
```

## 🎯 주요 개선점

### 1. **방별 독립성**
- 각 방마다 독립적인 RoundManager
- 메모리 누수 방지
- 동시성 안전성 확보

### 2. **설정 기반 게임 규칙**
- 외부 설정으로 게임 시간 조절
- 라운드 수 동적 변경 가능
- 조기 종료 설정 가능

### 3. **타입 안전성**
- String gameId → JoinCode 타입 사용
- 컴파일 타임 오류 방지

### 4. **테스트 용이성**
- 각 컴포넌트별 독립적 테스트
- Mock 객체 활용 쉬워짐

## ⚠️ 주의사항

### 1. **기존 코드 호환성**
- deprecated 클래스들은 빈 클래스로 유지
- 컴파일 오류는 발생하지 않음
- 런타임에 실제 기능은 동작하지 않음

### 2. **테스트 코드**
- 기존 테스트들은 새 시스템에 맞게 수정됨
- Task 관련 테스트들은 RoundManager 테스트로 대체

### 3. **설정 파일**
- `use-new-round-system` 플래그 제거됨
- 새 시스템만 사용

## 🚀 마이그레이션 체크리스트

- [x] 기존 Task 시스템 제거
- [x] 새로운 RoundManager 시스템 구축  
- [x] CardGame 클래스 새 방식으로 변경
- [x] PlayerHands, CardHand 새 메서드 추가
- [x] CardGameService 완전 교체
- [x] 테스트 코드 수정
- [x] 설정 파일 정리
- [x] deprecated 클래스 정리
- [x] 모니터링 API 추가

## 📞 문제 해결

### 컴파일 오류가 발생하는 경우
1. IDE에서 프로젝트 새로고침
2. `./gradlew clean build` 실행
3. deprecated 클래스 참조 확인

### 런타임 오류가 발생하는 경우  
1. 설정 파일 확인 (`application.yml`)
2. Bean 등록 상태 확인
3. 로그에서 RoundManager 관련 오류 확인

기존 시스템이 완전히 제거되어 더 깔끔하고 유지보수하기 쉬운 코드가 되었습니다! 🎉
