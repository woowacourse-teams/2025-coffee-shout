# WebSocket API

이 프로젝트의 WebSocket API는 STOMP 프로토콜을 사용하여 실시간 통신을 제공합니다.

## 🚀 주요 기능

### 1. Page Visibility API 기반 자동 재연결

- 앱이 백그라운드로 전환되면 자동으로 웹소켓 연결 해제
- 앱이 포그라운드로 돌아오면 자동으로 웹소켓 재연결
- 네트워크 안정화를 위해 1초 지연 후 재연결 시도

### 2. 자동 구독 관리

- `useWebSocketSubscription` 훅으로 자동 구독/해제
- 페이지 가시성에 따른 동적 구독 관리
- 컴포넌트 언마운트시 자동 정리

### 3. 에러 처리 및 로깅

- Sentry를 통한 에러 추적
- 상세한 콘솔 로깅
- 연결 상태 모니터링

### 4. 상태 공유 및 동기화

- WebSocketProvider에서 중앙 집중식 Page Visibility 상태 관리
- 모든 컴포넌트에서 동일한 가시성 상태 공유
- 불필요한 중복 이벤트 리스너 방지

### 5. 게임 상태 기반 재연결 정책

- 게임 진행 중: 재연결 금지 (백엔드에서 차단)
- 로비 상태: 재연결 허용
- 최대 재연결 시도 횟수 제한 (3회)
- 재연결 실패 시 사용자 알림

### 6. 상태 유지 vs 구독 재설정

- **Provider 상태**: 앱 전환/재연결과 무관하게 유지됨
- **웹소켓 구독**: 재연결시 자동으로 재설정됨
- **게임 상태 동기화**: 재연결 후 서버와 상태 동기화

### 7. 연결 파라미터 관리

- **STOMP 연결 헤더**: joinCode, playerName을 서버로 전송
- **재연결시 자동 전달**: Identifier 컨텍스트의 값으로 재연결 시도
- **자동 동기화**: useIdentifier의 값이 변경되면 자동으로 반영

## 📁 파일 구조

```
src/apis/websocket/
├── contexts/
│   ├── WebSocketContext.ts          # WebSocket 컨텍스트 타입 정의
│   └── WebSocketProvider.tsx        # WebSocket 프로바이더 (Page Visibility API 포함)
├── hooks/
│   ├── usePageVisibility.ts         # Page Visibility API 훅 (독립적 + 공유 버전)
│   ├── useWebSocketSubscription.ts  # WebSocket 구독 훅 (WebSocketProvider 상태 공유)
│   └── useReconnectionPolicy.ts     # 게임 상태 기반 재연결 정책 훅
├── utils/
│   └── getWebSocketUrl.ts           # WebSocket URL 생성
└── createStompClient.ts             # STOMP 클라이언트 생성
```

## 🎯 사용법

### 기본 WebSocket 사용

```tsx
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';

const MyComponent = () => {
  const { startSocket, stopSocket, send, isConnected } = useWebSocket();

  useEffect(() => {
    startSocket(); // 웹소켓 연결 시작
  }, []);

  const handleSendMessage = () => {
    send('/room/ABC123/message', { text: 'Hello!' });
  };

  return (
    <div>
      <p>연결 상태: {isConnected ? '연결됨' : '끊어짐'}</p>
      <button onClick={handleSendMessage}>메시지 전송</button>
    </div>
  );
};
```

### WebSocket 구독

```tsx
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';

const ChatComponent = () => {
  const [messages, setMessages] = useState([]);

  const handleNewMessage = (data) => {
    setMessages((prev) => [...prev, data]);
  };

  // 자동으로 구독/해제 관리됨
  useWebSocketSubscription('/room/ABC123/chat', handleNewMessage);

  return (
    <div>
      {messages.map((msg) => (
        <div key={msg.id}>{msg.text}</div>
      ))}
    </div>
  );
};
```

### Page Visibility API 사용

#### WebSocketProvider와 상태 공유 (권장)

```tsx
import { useWebSocketPageVisibility } from '@/apis/websocket/hooks/usePageVisibility';

const VisibilityAwareComponent = () => {
  const isVisible = useWebSocketPageVisibility();

  return (
    <div>
      <p>페이지 상태: {isVisible ? '보임' : '숨김'}</p>
    </div>
  );
};
```

#### 독립적인 Page Visibility (WebSocketProvider 외부에서 사용)

```tsx
import { usePageVisibility } from '@/apis/websocket/hooks/usePageVisibility';

const StandaloneVisibilityComponent = () => {
  const isVisible = usePageVisibility();

  return (
    <div>
      <p>페이지 상태: {isVisible ? '보임' : '숨김'}</p>
    </div>
  );
};
```

### 재연결 정책 사용

```tsx
import { useReconnectionPolicy } from '@/apis/websocket/hooks/useReconnectionPolicy';

const GameComponent = () => {
  const { currentPolicy, shouldReconnect, getReconnectionDelay } = useReconnectionPolicy();

  return (
    <div>
      <p>현재 재연결 정책: {currentPolicy}</p>
      <p>재연결 지연 시간: {getReconnectionDelay()}ms</p>
    </div>
  );
};
```

### 연결 파라미터 자동 관리

```tsx
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

const GameComponent = () => {
  const { startSocket } = useWebSocket();
  const { joinCode, myName } = useIdentifier();

  const handleJoinGame = () => {
    // useIdentifier의 값이 자동으로 연결 파라미터로 사용됨
    startSocket();
  };

  return <button onClick={handleJoinGame}>게임 참가</button>;
};
```

## 🔄 자동 재연결 동작

### 백그라운드 전환 시

1. Page Visibility API가 `hidden` 상태 감지
2. 현재 웹소켓 연결 상태 저장
3. 웹소켓 연결 해제
4. 모든 구독 자동 해제

### 포그라운드 복귀 시

1. Page Visibility API가 `visible` 상태 감지
2. 재연결 정책에 따라 재연결 여부 결정
3. 최대 재연결 시도 횟수 체크 (3회)
4. 정책에 따른 지연 시간 후 재연결 시도
5. 모든 구독 자동 복구

## 🎮 재연결 정책

### ALWAYS (항상 재연결)

- **적용 상황**: 기본 상태, 게임 준비 중
- **동작**: 앱 전환 시 항상 재연결 시도
- **지연 시간**: 1초

### LOBBY_ONLY (로비에서만 재연결)

- **적용 상황**: 로비 페이지
- **동작**: 로비에서만 재연결 허용
- **지연 시간**: 1초

### NEVER (재연결 금지)

- **적용 상황**: 게임 진행 중 (`/play` 경로 + `PLAYING` 상태)
- **동작**: 앱 전환 시 재연결하지 않음
- **지연 시간**: 0초 (즉시 중단)

## 🔄 상태 유지 vs 구독 재설정

### Provider 상태 유지

```tsx
// CardGameProvider의 상태들 - 앱 전환/재연결과 무관하게 유지됨
const [currentCardGameState, setCurrentCardGameState] = useState<CardGameState>('READY');
const [currentRound, setCurrentRound] = useState<RoundKey>(1);
const [cardInfos, setCardInfos] = useState<CardInfo[]>([]);
```

### 웹소켓 구독 재설정

```tsx
// 재연결시 자동으로 재설정됨
useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);
```

### 상태 동기화 시나리오

1. **게임 진행 중 앱 전환**: Provider 상태는 유지, 구독은 해제
2. **앱 복귀**: 재연결 정책에 따라 구독 재설정 여부 결정
3. **구독 재설정**: 서버에서 현재 게임 상태를 다시 받아옴
4. **상태 동기화**: Provider 상태와 서버 상태가 동기화됨

## 📊 로깅

### 연결 관련 로그

- `✅WebSocket 연결` - 연결 성공
- `❌WebSocket 연결 해제` - 연결 해제
- `📱 앱이 백그라운드로 전환됨 - 웹소켓 연결 해제 (정책: POLICY)` - 백그라운드 전환
- `📱 앱이 포그라운드로 전환됨 - 웹소켓 재연결 시도 (정책: POLICY, 시도: N/M)` - 포그라운드 복귀
- `🔄 웹소켓 재연결 시작 (정책: POLICY)` - 재연결 시도
- `❌ 최대 재연결 시도 횟수 초과 (3회) - 재연결 중단` - 재연결 실패
- `🔧 연결 파라미터 설정: joinCode=ABC123, playerName=Player1` - 연결 파라미터 설정 (제거됨)

### 구독 관련 로그

- `✅ 웹소켓 구독 성공: /destination` - 구독 성공
- `🔌 웹소켓 구독 해제: /destination` - 구독 해제
- `❌ 웹소켓 구독 실패: error` - 구독 실패

## 🛠️ 설정

### WebSocket URL

`utils/getWebSocketUrl.ts`에서 환경별 WebSocket URL을 설정할 수 있습니다.

### 재연결 지연 시간

`WebSocketProvider.tsx`에서 재연결 지연 시간을 조정할 수 있습니다:

```tsx
// 기본값: 1000ms (1초)
reconnectTimeoutRef.current = window.setTimeout(() => {
  startSocket();
}, 1000);
```

## 🐛 문제 해결

### 연결이 안되는 경우

1. 네트워크 연결 확인
2. WebSocket URL 설정 확인
3. 브라우저 콘솔에서 에러 로그 확인

### 재연결이 안되는 경우

1. Page Visibility API 지원 여부 확인
2. 브라우저 탭 전환 테스트
3. 콘솔 로그에서 재연결 시도 확인

### 구독이 안되는 경우

1. 웹소켓 연결 상태 확인
2. 페이지 가시성 상태 확인
3. 구독 대상 경로 확인

### Page Visibility 상태가 동기화되지 않는 경우

1. `useWebSocketPageVisibility` 사용 여부 확인
2. WebSocketProvider 내부에서 관리되는 상태인지 확인
3. 독립적인 `usePageVisibility` 사용 시 상태 공유되지 않음 주의

### 재연결이 예상과 다르게 동작하는 경우

1. 현재 재연결 정책 확인 (`currentPolicy`)
2. 게임 상태와 경로가 정책과 일치하는지 확인
3. 최대 재연결 시도 횟수 초과 여부 확인
4. 백엔드에서 재연결을 차단하고 있는지 확인
