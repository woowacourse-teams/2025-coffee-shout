import { wait, DELAY_BETWEEN_ACTIONS } from './domUtils';
import {
  findPageAction,
  handleHostGameStart,
  clearRacingGameClickInterval,
  type PageActionContext,
} from './pageActions';
import { MiniGameType } from '@/types/miniGame/common';
import { getAutoTestLogger } from '../utils/autoTestLogger';
import { TestMessage } from '@/devtools/autoTest/types/testMessage';

// 플로우 상태 추적 (각 역할별로)
type FlowState = 'idle' | 'running' | 'paused';
const flowState: Record<'host' | 'guest', FlowState> = {
  host: 'idle',
  guest: 'idle',
};

// 역할별 상태 접근 헬퍼 함수 (타입 추론 개선)
const getFlowState = (role: 'host' | 'guest'): FlowState => flowState[role];
const setFlowState = (role: 'host' | 'guest', state: FlowState): void => {
  flowState[role] = state;
};

// 컨텍스트 추출 헬퍼
const getContext = (): string => {
  if (typeof window === 'undefined') return 'MAIN';
  try {
    const iframeName = window.frameElement?.getAttribute('name') || '';
    return iframeName || 'MAIN';
  } catch {
    return 'MAIN';
  }
};

// 페이지 기반 플로우 실행
const runFlow = async (role: 'host' | 'guest', context: PageActionContext) => {
  const logger = getAutoTestLogger();
  const currentContext = getContext();

  logger.addLog({
    message: `${role} 플로우 실행 호출`,
    context: currentContext,
    data: {
      currentPath: window.location.pathname,
      flowState: getFlowState(role),
    },
  });

  // 이미 실행 중이면 무시
  const currentState = getFlowState(role);
  if (currentState === 'running' || currentState === 'paused') {
    logger.addLog({
      message: `${role} 플로우가 이미 실행 중, 건너뜀`,
      context: currentContext,
    });
    return;
  }

  setFlowState(role, 'running');
  logger.addLog({
    message: `${role} 플로우 시작, flowState 업데이트됨`,
    context: currentContext,
  });

  try {
    logger.addLog({
      message: `초기 대기 시간: ${DELAY_BETWEEN_ACTIONS}ms`,
      context: currentContext,
    });
    await wait(DELAY_BETWEEN_ACTIONS);

    // 현재 경로에서 시작
    let currentPath = window.location.pathname;
    let joinCode: string | null = null;
    logger.addLog({
      message: `경로에서 플로우 시작: ${currentPath}`,
      context: currentContext,
    });

    // 게스트의 경우 joinCode를 사용해야 하는 페이지 처리
    if (role === 'guest' && currentPath === '/') {
      logger.addLog({
        message: '게스트가 홈 페이지에 있음, joinCode 대기 중일 수 있음',
        context: currentContext,
      });
      // joinCode 입력 페이지로 이동해야 함 (이미 homePageGuestAction에서 처리됨)
      // 하지만 joinCode는 나중에 받을 수 있으므로 대기
    }

    // 플로우 실행 루프
    logger.addLog({
      message: `${role} 플로우 루프 진입`,
      context: currentContext,
    });
    let loopIteration = 0;
    while (true) {
      const currentFlowState = getFlowState(role);
      if (currentFlowState !== 'running' && currentFlowState !== 'paused') {
        break;
      }

      loopIteration++;
      logger.addLog({
        message: `${role} 플로우 루프 반복 ${loopIteration}`,
        context: currentContext,
      });
      // joinCode 업데이트 (경로에서 추출)
      const pathJoinCodeMatch = currentPath.match(/^\/room\/([^/]+)/);
      if (pathJoinCodeMatch) {
        joinCode = pathJoinCodeMatch[1];
        context.joinCode = joinCode;
        logger.addLog({
          message: `경로에서 joinCode 추출: ${joinCode}`,
          context: currentContext,
        });
      }

      // 현재 경로에 맞는 액션 찾기
      logger.addLog({
        message: `경로에 대한 페이지 액션 찾는 중: ${currentPath}, 역할: ${role}`,
        context: currentContext,
      });
      const pageAction = findPageAction(currentPath, role);

      if (pageAction) {
        logger.addLog({
          message: '페이지 액션 찾음, 실행 중...',
          context: currentContext,
          data: {
            currentPath,
            role,
            actionName: pageAction.constructor?.name || 'unknown',
          },
        });
        await pageAction.execute(context);
        logger.addLog({
          message: '페이지 액션 실행 완료',
          context: currentContext,
        });
      } else {
        // 액션이 없으면 현재 경로에서 대기 (다른 이벤트 처리 대기)
        logger.addLog({
          message: `경로에 대한 액션 없음: ${currentPath}, 역할: ${role}`,
          context: currentContext,
        });
      }

      // 일시 중지 상태 체크 (페이지 액션 실행 후)
      // 상태가 변경되었을 수 있으므로 다시 확인
      const checkPausedState = getFlowState(role);
      if (checkPausedState === 'paused') {
        logger.addLog({
          message: `${role} 플로우 일시 중지됨 (페이지 액션 후), 재개 대기 중...`,
          context: currentContext,
        });
        await waitForResume(role);
        logger.addLog({
          message: `${role} 플로우 재개됨`,
          context: currentContext,
        });
      }

      // 경로 변경 대기 (타임아웃 없음)
      logger.addLog({
        message: `경로 변경 대기 중: ${currentPath}`,
        context: currentContext,
      });
      await waitForPathChange(currentPath, role);

      const newPath = window.location.pathname;
      logger.addLog({
        message: `경로 변경됨: ${currentPath} -> ${newPath}`,
        context: currentContext,
      });

      // 레이싱 게임 페이지를 벗어나면 클릭 인터벌 정리
      if (currentPath.match(/^\/room\/[^/]+\/RACING_GAME\/play$/)) {
        clearRacingGameClickInterval();
      }

      // 주문 페이지에 도달하면 완료
      if (/^\/room\/[^/]+\/order$/.test(newPath)) {
        logger.addLog({
          message: '주문 페이지 도달, 테스트 완료',
          context: currentContext,
        });
        setFlowState(role, 'idle');
        break;
      }

      // 홈으로 리셋된 경우 플로우 종료 (테스트 완료 후 리셋)
      if (newPath === '/') {
        logger.addLog({
          message: '홈으로 리셋, 플로우 완료',
          context: currentContext,
        });
        clearRacingGameClickInterval();
        setFlowState(role, 'idle');
        break;
      }

      currentPath = newPath;
    }
    logger.addLog({
      message: `${role} 플로우 루프 종료`,
      context: currentContext,
    });
  } catch (error) {
    console.error(`[AutoTest Debug] Error in ${role} flow:`, error);
    logger.addLog({
      message: `${role} 플로우 오류: ${error instanceof Error ? error.message : String(error)}`,
      context: currentContext,
      data: error,
    });
    throw error;
  } finally {
    setFlowState(role, 'idle');
    logger.addLog({
      message: `${role} 플로우 완료, flowState 리셋됨`,
      context: currentContext,
    });
  }
};

// 경로 변경 대기 (타임아웃 없음)
const waitForPathChange = async (currentPath: string, role: 'host' | 'guest'): Promise<void> => {
  const logger = getAutoTestLogger();
  const currentContext = getContext();

  while (window.location.pathname === currentPath) {
    // 일시 중지 상태 체크
    if (getFlowState(role) === 'paused') {
      logger.addLog({
        message: `${role} 경로 변경 대기 일시 중지됨`,
        context: currentContext,
      });
      await waitForResume(role);
      logger.addLog({
        message: `${role} 경로 변경 대기 재개됨`,
        context: currentContext,
      });
    }
    await wait(100);
  }
  await wait(500); // 경로 변경 후 안정화 대기
};

// 재개 신호 대기
const waitForResume = async (role: 'host' | 'guest'): Promise<void> => {
  while (getFlowState(role) === 'paused') {
    await wait(100);
  }
};

// 호스트 플로우 실행
const runHostFlow = async (gameSequence?: MiniGameType[]) => {
  const logger = getAutoTestLogger();
  const currentContext = getContext();

  logger.addLog({
    message: '호스트 플로우 실행 호출',
    context: currentContext,
    data: { gameSequence },
  });

  const context: PageActionContext = {
    role: 'host',
    playerName: 'host',
    gameSequence,
  };

  logger.addLog({
    message: '호스트 컨텍스트 생성',
    context: currentContext,
    data: context,
  });

  await runFlow('host', context);
};

// 게스트 플로우 실행
const runGuestFlow = async (joinCode: string, iframeName?: string) => {
  const logger = getAutoTestLogger();
  const currentContext = getContext();

  logger.addLog({
    message: '게스트 플로우 실행 호출',
    context: currentContext,
    data: { joinCode, iframeName },
  });

  const guestName = iframeName || 'guest1';
  const context: PageActionContext = {
    role: 'guest',
    joinCode,
    playerName: guestName,
    iframeName,
  };

  logger.addLog({
    message: '게스트 컨텍스트 생성',
    context: currentContext,
    data: context,
  });

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
      clearRacingGameClickInterval();
      setFlowState('host', 'idle');
      setFlowState('guest', 'idle');
    } else if (event.data.type === 'STOP_TEST') {
      // 모든 플로우 즉시 종료
      clearRacingGameClickInterval();
      setFlowState('host', 'idle');
      setFlowState('guest', 'idle');
    } else if (event.data.type === 'PAUSE_TEST') {
      // 모든 플로우 일시 중지
      if (getFlowState('host') === 'running') {
        setFlowState('host', 'paused');
      }
      if (getFlowState('guest') === 'running') {
        setFlowState('guest', 'paused');
      }
      const logger = getAutoTestLogger();
      logger.addLog({
        message: '모든 역할의 테스트 일시 중지됨',
        context: getContext(),
      });
    } else if (event.data.type === 'RESUME_TEST') {
      // 모든 플로우 재개
      if (getFlowState('host') === 'paused') {
        setFlowState('host', 'running');
      }
      if (getFlowState('guest') === 'paused') {
        setFlowState('guest', 'running');
      }
      const logger = getAutoTestLogger();
      logger.addLog({
        message: '모든 역할의 테스트 재개됨',
        context: getContext(),
      });
    } else if (event.data.type === 'RESET_TO_HOME') {
      window.location.href = '/';
    }
  };

  window.addEventListener('message', messageHandler);

  return () => {
    window.removeEventListener('message', messageHandler);
  };
};
