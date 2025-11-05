import { wait, DELAY_BETWEEN_ACTIONS } from './domUtils';
import { findPageAction, handleHostGameStart, type PageActionContext } from './pageActions';
import { MiniGameType } from '@/types/miniGame/common';

type TestMessage =
  | {
      type: 'START_TEST';
      role: 'host' | 'guest';
      joinCode?: string;
      iframeName?: string;
      gameSequence: MiniGameType[];
    }
  | { type: 'JOIN_CODE_RECEIVED'; joinCode: string }
  | { type: 'GUEST_READY'; iframeName?: string }
  | { type: 'CLICK_GAME_START' }
  | { type: 'TEST_COMPLETED' }
  | { type: 'STOP_TEST' }
  | { type: 'RESET_TO_HOME' };

// 플로우 실행 상태 추적 (각 역할별로)
const flowRunningState: { [role: string]: boolean } = {};

// 페이지 기반 플로우 실행
const runFlow = async (role: 'host' | 'guest', context: PageActionContext) => {
  console.log(`[AutoTest Debug] runFlow called for ${role}`, {
    role,
    context,
    currentPath: window.location.pathname,
    flowRunningState: flowRunningState[role],
  });

  // 이미 실행 중이면 무시
  if (flowRunningState[role]) {
    console.log(`[AutoTest Debug] Flow already running for ${role}, skipping`);
    return;
  }

  flowRunningState[role] = true;
  console.log(`[AutoTest Debug] ${role} flow started, flowRunningState updated`);

  try {
    console.log(`[AutoTest Debug] Waiting initial delay: ${DELAY_BETWEEN_ACTIONS}ms`);
    await wait(DELAY_BETWEEN_ACTIONS);

    // 현재 경로에서 시작
    let currentPath = window.location.pathname;
    let joinCode: string | null = null;
    console.log(`[AutoTest Debug] Starting flow from path: ${currentPath}`);

    // 게스트의 경우 joinCode를 사용해야 하는 페이지 처리
    if (role === 'guest' && currentPath === '/') {
      console.log('[AutoTest Debug] Guest at home page, may need to wait for joinCode');
      // joinCode 입력 페이지로 이동해야 함 (이미 homePageGuestAction에서 처리됨)
      // 하지만 joinCode는 나중에 받을 수 있으므로 대기
    }

    // 플로우 실행 루프
    console.log(`[AutoTest Debug] Entering flow loop for ${role}`);
    let loopIteration = 0;
    while (flowRunningState[role]) {
      loopIteration++;
      console.log(`[AutoTest Debug] Flow loop iteration ${loopIteration} for ${role}`);
      // joinCode 업데이트 (경로에서 추출)
      const pathJoinCodeMatch = currentPath.match(/^\/room\/([^/]+)/);
      if (pathJoinCodeMatch) {
        joinCode = pathJoinCodeMatch[1];
        context.joinCode = joinCode;
        console.log(`[AutoTest Debug] Extracted joinCode from path: ${joinCode}`);
      }

      // 현재 경로에 맞는 액션 찾기
      console.log(`[AutoTest Debug] Finding page action for path: ${currentPath}, role: ${role}`);
      const pageAction = findPageAction(currentPath, role);

      if (pageAction) {
        console.log(`[AutoTest Debug] Found page action, executing...`, {
          currentPath,
          role,
          actionName: pageAction.constructor?.name || 'unknown',
        });
        await pageAction.execute(context);
        console.log(`[AutoTest Debug] Page action executed successfully`);
      } else {
        // 액션이 없으면 현재 경로에서 대기 (다른 이벤트 처리 대기)
        console.log(`[AutoTest Debug] No action found for path: ${currentPath}, role: ${role}`);
      }

      // 경로 변경 대기 (최대 30초)
      console.log(`[AutoTest Debug] Waiting for path change from: ${currentPath}`);
      const pathChanged = await waitForPathChangeWithTimeout(currentPath, 30000);
      if (!pathChanged) {
        // 경로가 변경되지 않았으면 종료
        console.log('[AutoTest Debug] Path did not change after timeout, stopping flow');
        flowRunningState[role] = false;
        break;
      }

      const newPath = window.location.pathname;
      console.log(`[AutoTest Debug] Path changed: ${currentPath} -> ${newPath}`);

      // 주문 페이지에 도달하면 완료
      if (/^\/room\/[^/]+\/order$/.test(newPath)) {
        console.log('[AutoTest Debug] Reached order page, test completed');
        flowRunningState[role] = false;
        break;
      }

      // 홈으로 리셋된 경우 플로우 종료 (테스트 완료 후 리셋)
      if (newPath === '/') {
        console.log('[AutoTest Debug] Reset to home, flow completed');
        flowRunningState[role] = false;
        break;
      }

      currentPath = newPath;
    }
    console.log(`[AutoTest Debug] Flow loop exited for ${role}`);
  } catch (error) {
    console.error(`[AutoTest Debug] Error in ${role} flow:`, error);
    throw error;
  } finally {
    flowRunningState[role] = false;
    console.log(`[AutoTest Debug] ${role} flow completed, flowRunningState reset`);
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
const runHostFlow = async (gameSequence?: MiniGameType[]) => {
  console.log('[AutoTest Debug] runHostFlow called', { gameSequence });
  const context: PageActionContext = {
    role: 'host',
    playerName: 'host',
    gameSequence,
  };
  console.log('[AutoTest Debug] Creating host context:', context);
  await runFlow('host', context);
};

// 게스트 플로우 실행
const runGuestFlow = async (joinCode: string, iframeName?: string) => {
  console.log('[AutoTest Debug] runGuestFlow called', { joinCode, iframeName });
  const guestName = iframeName || 'guest1';
  const context: PageActionContext = {
    role: 'guest',
    joinCode,
    playerName: guestName,
    iframeName,
  };
  console.log('[AutoTest Debug] Creating guest context:', context);
  await runFlow('guest', context);
};

export const setupAutoTestListener = () => {
  if (typeof window === 'undefined') return;

  let guestJoinCode: string | null = null;

  const messageHandler = async (event: MessageEvent<TestMessage>) => {
    // TestMessage 타입 체크
    if (!event.data || typeof event.data !== 'object' || !('type' in event.data)) {
      return;
    }

    if (event.data.type === 'START_TEST') {
      const { role, joinCode, iframeName, gameSequence } = event.data;

      if (role === 'host') {
        runHostFlow(gameSequence);
      } else if (role === 'guest') {
        if (joinCode) {
          guestJoinCode = joinCode;
          runGuestFlow(joinCode, iframeName);
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
      // 모든 플로우 종료
      flowRunningState.host = false;
      flowRunningState.guest = false;
    } else if (event.data.type === 'STOP_TEST') {
      // 모든 플로우 즉시 종료
      flowRunningState.host = false;
      flowRunningState.guest = false;
    } else if (event.data.type === 'RESET_TO_HOME') {
      window.location.href = '/';
    }
  };

  window.addEventListener('message', messageHandler);

  return () => {
    window.removeEventListener('message', messageHandler);
  };
};
