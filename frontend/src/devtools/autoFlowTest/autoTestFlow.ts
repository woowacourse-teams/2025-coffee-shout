import { wait, DELAY_BETWEEN_ACTIONS } from './domUtils';
import { findPageAction, handleHostGameStart, type PageActionContext } from './pageActions';

type TestMessage =
  | { type: 'START_TEST'; role: 'host' | 'guest'; joinCode?: string; iframeName?: string }
  | { type: 'JOIN_CODE_RECEIVED'; joinCode: string }
  | { type: 'GUEST_READY'; iframeName?: string }
  | { type: 'CLICK_GAME_START' }
  | { type: 'TEST_COMPLETED' }
  | { type: 'RESET_TO_HOME' };

// 플로우 실행 상태 추적 (각 역할별로)
const flowRunningState: { [role: string]: boolean } = {};

// 페이지 기반 플로우 실행
const runFlow = async (role: 'host' | 'guest', context: PageActionContext) => {
  // 이미 실행 중이면 무시
  if (flowRunningState[role]) {
    console.log(`[AutoTest] Flow already running for ${role}, skipping`);
    return;
  }

  flowRunningState[role] = true;
  console.log(`[AutoTest] ${role} flow started`);

  try {
    await wait(DELAY_BETWEEN_ACTIONS);

    // 현재 경로에서 시작
    let currentPath = window.location.pathname;
    let joinCode: string | null = null;

    // 게스트의 경우 joinCode를 사용해야 하는 페이지 처리
    if (role === 'guest' && currentPath === '/') {
      // joinCode 입력 페이지로 이동해야 함 (이미 homePageGuestAction에서 처리됨)
      // 하지만 joinCode는 나중에 받을 수 있으므로 대기
    }

    // 플로우 실행 루프
    while (flowRunningState[role]) {
      // joinCode 업데이트 (경로에서 추출)
      const pathJoinCodeMatch = currentPath.match(/^\/room\/([^/]+)/);
      if (pathJoinCodeMatch) {
        joinCode = pathJoinCodeMatch[1];
        context.joinCode = joinCode;
      }

      // 현재 경로에 맞는 액션 찾기
      const pageAction = findPageAction(currentPath, role);

      if (pageAction) {
        console.log(`[AutoTest] Executing action for path: ${currentPath}, role: ${role}`);
        await pageAction.execute(context);
      } else {
        // 액션이 없으면 현재 경로에서 대기 (다른 이벤트 처리 대기)
        console.log(`[AutoTest] No action found for path: ${currentPath}, role: ${role}`);
      }

      // 경로 변경 대기 (최대 30초)
      const pathChanged = await waitForPathChangeWithTimeout(currentPath, 30000);
      if (!pathChanged) {
        // 경로가 변경되지 않았으면 종료
        console.log('[AutoTest] Path did not change, flow may be complete or waiting for event');
        flowRunningState[role] = false;
        break;
      }

      const newPath = window.location.pathname;
      console.log(`[AutoTest] Path changed: ${currentPath} -> ${newPath}`);

      // 주문 페이지에 도달하면 완료
      if (/^\/room\/[^/]+\/order$/.test(newPath)) {
        console.log('[AutoTest] Reached order page, test completed');
        flowRunningState[role] = false;
        break;
      }

      // 홈으로 리셋된 경우 플로우 종료 (테스트 완료 후 리셋)
      if (newPath === '/') {
        console.log('[AutoTest] Reset to home, flow completed');
        flowRunningState[role] = false;
        break;
      }

      currentPath = newPath;
    }
  } finally {
    flowRunningState[role] = false;
    console.log(`[AutoTest] ${role} flow completed`);
  }
};

// 경로 변경 대기 (타임아웃 포함)
const waitForPathChangeWithTimeout = async (
  currentPath: string,
  maxWait: number
): Promise<boolean> => {
  const startTime = Date.now();
  while (Date.now() - startTime < maxWait) {
    if (window.location.pathname !== currentPath) {
      await wait(500);
      return true;
    }
    await wait(100);
  }
  return false;
};

// 호스트 플로우 실행
const runHostFlow = async () => {
  const context: PageActionContext = {
    role: 'host',
    playerName: 'host',
  };
  await runFlow('host', context);
};

// 게스트 플로우 실행
const runGuestFlow = async (joinCode: string, iframeName?: string) => {
  const guestName = iframeName || 'guest1';
  const context: PageActionContext = {
    role: 'guest',
    joinCode,
    playerName: guestName,
    iframeName,
  };
  await runFlow('guest', context);
};

export const setupAutoTestListener = () => {
  if (typeof window === 'undefined') return;

  let guestJoinCode: string | null = null;

  const messageHandler = async (event: MessageEvent<TestMessage>) => {
    if (event.data.type === 'START_TEST') {
      const { role, joinCode, iframeName } = event.data;

      if (role === 'host') {
        runHostFlow();
      } else if (role === 'guest') {
        if (joinCode) {
          guestJoinCode = joinCode;
          runGuestFlow(joinCode, iframeName);
        } else {
          console.log('[AutoTest] Guest waiting for joinCode...');
        }
      }
    } else if (event.data.type === 'JOIN_CODE_RECEIVED') {
      const { joinCode } = event.data;
      if (guestJoinCode === null) {
        guestJoinCode = joinCode;
        const iframeName = window.frameElement?.getAttribute('name') || '';
        runGuestFlow(joinCode, iframeName);
      }
    } else if (event.data.type === 'CLICK_GAME_START') {
      await handleHostGameStart();
    } else if (event.data.type === 'TEST_COMPLETED') {
      console.log('[AutoTest] Test completed, stopping all flows');
      // 모든 플로우 종료
      flowRunningState.host = false;
      flowRunningState.guest = false;
    } else if (event.data.type === 'RESET_TO_HOME') {
      // 리셋 시 플로우 상태는 유지 (새 테스트를 위해 초기화하지 않음)
      window.location.href = '/';
    }
  };

  window.addEventListener('message', messageHandler);

  return () => {
    window.removeEventListener('message', messageHandler);
  };
};
