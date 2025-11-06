import { useEffect, useMemo, useRef, useState, type MouseEvent } from 'react';
import { useLocation } from 'react-router-dom';
import { checkIsTouchDevice } from '../../../../utils/checkIsTouchDevice';
import { MiniGameType, MINI_GAME_NAME_MAP } from '@/types/miniGame/common';
import { getAutoTestLogger } from '../../utils/autoTestLogger';
import AutoTestLogPanel from '../AutoTestLogPanel/AutoTestLogPanel';
import * as S from './IframePreviewToggle.styled';

type TestMessage =
  | { type: 'START_TEST'; role: 'host' | 'guest'; joinCode?: string; gameSequence: MiniGameType[] }
  | { type: 'JOIN_CODE_RECEIVED'; joinCode: string }
  | { type: 'GUEST_READY'; iframeName?: string }
  | { type: 'CLICK_GAME_START' }
  | { type: 'PATH_CHANGE'; iframeName: string; path: string }
  | { type: 'TEST_COMPLETED' }
  | { type: 'STOP_TEST' }
  | { type: 'PAUSE_TEST' }
  | { type: 'RESUME_TEST' }
  | { type: 'RESET_TO_HOME' }
  | { type: 'READY'; iframeName: string };

const IframePreviewToggle = () => {
  const location = useLocation();
  const [open, setOpen] = useState<boolean>(false);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [iframeNames, setIframeNames] = useState<string[]>(['host', 'guest1']);
  const [iframePaths, setIframePaths] = useState<{ [key: string]: string }>({});
  const [guestReadyState, setGuestReadyState] = useState<{ [guestName: string]: boolean }>({});
  // readyState는 READY 신호 추적용 (디버깅/로깅 목적)
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [readyState, setReadyState] = useState<{ [iframeName: string]: boolean }>({});
  const [gameSequence, setGameSequence] = useState<MiniGameType[]>(['CARD_GAME']);
  const [isGameSelectionExpanded, setIsGameSelectionExpanded] = useState<boolean>(false);
  const iframeRefs = useRef<{ [key: string]: HTMLIFrameElement | null }>({});
  const joinCodeRef = useRef<string | null>(null);
  const pendingStartTest = useRef<boolean>(false);

  // 사용 가능한 게임 목록 (하드코딩, fetch 없이)
  const availableGames = useMemo(() => {
    return Object.keys(MINI_GAME_NAME_MAP) as MiniGameType[];
  }, []);

  const isTopWindow = useMemo(() => {
    if (typeof window === 'undefined') return false;
    try {
      return window.self === window.top;
    } catch {
      return false;
    }
  }, []);

  const isTouchDevice = useMemo(() => checkIsTouchDevice(), []);
  const isRootPath = location.pathname === '/';

  useEffect(() => {
    // 경로가 바뀌면 닫아준다 (예상치 못한 잔상 방지)
    setOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!open) return;

    const handleMessage = (event: MessageEvent<TestMessage>) => {
      if (event.data.type === 'JOIN_CODE_RECEIVED') {
        const { joinCode } = event.data;
        joinCodeRef.current = joinCode;

        const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
        guestNames.forEach((guestName) => {
          const guestIframe = iframeRefs.current[guestName];
          if (guestIframe?.contentWindow) {
            guestIframe.contentWindow.postMessage(
              {
                type: 'START_TEST',
                role: 'guest',
                joinCode,
                iframeName: guestName,
                gameSequence,
              },
              '*'
            );
          }
        });
      } else if (event.data.type === 'READY') {
        const { iframeName } = event.data;
        const logger = getAutoTestLogger();
        logger.addLog({
          message: 'iframe에서 READY 신호 수신',
          context: 'MAIN',
          data: { iframeName, pendingStartTest: pendingStartTest.current },
        });
        setReadyState((prev) => ({
          ...prev,
          [iframeName]: true,
        }));

        // READY 신호를 받고 START_TEST가 대기 중이면 즉시 전송
        if (pendingStartTest.current && iframeName === 'host') {
          logger.addLog({
            message: '조건 충족, START_TEST 전송',
            context: 'MAIN',
          });
          setTimeout(() => {
            const hostIframe = iframeRefs.current.host;
            if (hostIframe?.contentWindow) {
              const message = {
                type: 'START_TEST' as const,
                role: 'host' as const,
                gameSequence,
              };
              logger.addLog({
                message: 'READY 후 host에 START_TEST 전송',
                context: 'MAIN',
                data: { gameSequence },
              });
              hostIframe.contentWindow.postMessage(message, '*');
              pendingStartTest.current = false;
            }
          }, 0);
        } else {
          logger.addLog({
            message: 'START_TEST 전송하지 않음',
            context: 'MAIN',
            data: {
              pendingStartTest: pendingStartTest.current,
              iframeName,
              isHost: iframeName === 'host',
            },
          });
        }
      } else if (event.data.type === 'GUEST_READY') {
        const { iframeName } = event.data;
        if (iframeName) {
          setGuestReadyState((prev) => ({
            ...prev,
            [iframeName]: true,
          }));
        }
      } else if (event.data.type === 'PATH_CHANGE') {
        const { iframeName, path } = event.data;
        setIframePaths((prev) => ({
          ...prev,
          [iframeName]: path,
        }));
      } else if (event.data.type === 'TEST_COMPLETED') {
        setIsRunning(false);
        setIsPaused(false);
      }
    };

    window.addEventListener('message', handleMessage);

    return () => {
      window.removeEventListener('message', handleMessage);
    };
  }, [open, iframeNames, gameSequence]);

  useEffect(() => {
    if (!open) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const allGuestsReady =
      guestNames.length > 0 && guestNames.every((guestName) => guestReadyState[guestName] === true);

    if (allGuestsReady) {
      const hostIframe = iframeRefs.current.host;
      if (hostIframe?.contentWindow) {
        hostIframe.contentWindow.postMessage({ type: 'CLICK_GAME_START' }, '*');
      }
    }
  }, [open, iframeNames, guestReadyState]);

  const iframeHeight = useMemo(() => {
    return iframeNames.length <= 4 ? '100%' : 'auto';
  }, [iframeNames.length]);

  const useMinHeight = useMemo(() => {
    return iframeNames.length > 4;
  }, [iframeNames.length]);

  const canAddMore = useMemo(() => {
    return iframeNames.length < 9;
  }, [iframeNames.length]);

  const handleAddIframe = () => {
    if (!canAddMore) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    const usedNumbers = new Set<number>();
    guestNames.forEach((name) => {
      const match = name.match(/^guest(\d+)$/);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num >= 1 && num <= 8) {
          usedNumbers.add(num);
        }
      }
    });

    // 1~8 중 사용 가능한 가장 작은 번호 찾기
    let nextGuestNumber: number | null = null;
    for (let i = 1; i <= 8; i++) {
      if (!usedNumbers.has(i)) {
        nextGuestNumber = i;
        break;
      }
    }

    if (nextGuestNumber === null) {
      // 1~8이 모두 사용 중
      return;
    }

    const newGuestName = `guest${nextGuestNumber}`;
    setIframeNames((prev) => [...prev, newGuestName]);
  };

  const handleDeleteIframe = (name: string) => {
    if (name === 'host' || name === 'guest1') return;

    // 맨 마지막 iframe만 삭제 가능
    const lastIndex = iframeNames.length - 1;
    const lastIframeName = iframeNames[lastIndex];
    if (name !== lastIframeName) return;

    setIframeNames((prev) => prev.filter((n) => n !== name));
    delete iframeRefs.current[name];
    setGuestReadyState((prev) => {
      const newState = { ...prev };
      delete newState[name];
      return newState;
    });
  };

  const handleGameToggle = (gameType: MiniGameType) => {
    setGameSequence((prev) => {
      if (prev.includes(gameType)) {
        // 이미 선택된 게임이면 제거
        return prev.filter((g) => g !== gameType);
      } else {
        // 선택되지 않은 게임이면 추가
        return [...prev, gameType];
      }
    });
  };

  const handleStartTest = () => {
    const logger = getAutoTestLogger();
    logger.addLog({
      message: 'handleStartTest 호출됨',
      context: 'MAIN',
    });
    setIsRunning(true);
    joinCodeRef.current = null;
    setGuestReadyState({});
    setReadyState({}); // RESET_TO_HOME 후 새로운 READY 신호를 기다리기 위해 초기화
    pendingStartTest.current = true; // START_TEST 전송 대기 플래그
    logger.addLog({
      message: 'pendingStartTest를 true로 설정',
      context: 'MAIN',
    });

    // 모든 iframe에 RESET_TO_HOME 전송
    iframeNames.forEach((name) => {
      const iframe = iframeRefs.current[name];
      if (iframe?.contentWindow) {
        logger.addLog({
          message: `${name}에 RESET_TO_HOME 전송`,
          context: 'MAIN',
        });
        iframe.contentWindow.postMessage({ type: 'RESET_TO_HOME' }, '*');
      }
    });

    // RESET 후 새로운 READY 신호를 기다림 (handleMessage에서 처리)
    // READY 신호가 이미 와있어도 RESET 후 다시 올 것이므로 대기
  };

  const handleStopTest = () => {
    setIsRunning(false);
    setIsPaused(false);

    iframeNames.forEach((name) => {
      const iframe = iframeRefs.current[name];
      if (iframe?.contentWindow) {
        iframe.contentWindow.postMessage({ type: 'STOP_TEST' }, '*');
      }
    });
  };

  const handlePauseTest = () => {
    setIsPaused(true);

    iframeNames.forEach((name) => {
      const iframe = iframeRefs.current[name];
      if (iframe?.contentWindow) {
        iframe.contentWindow.postMessage({ type: 'PAUSE_TEST' }, '*');
      }
    });
  };

  const handleResumeTest = () => {
    setIsPaused(false);

    iframeNames.forEach((name) => {
      const iframe = iframeRefs.current[name];
      if (iframe?.contentWindow) {
        iframe.contentWindow.postMessage({ type: 'RESUME_TEST' }, '*');
      }
    });
  };

  if (!isTopWindow || !isRootPath || isTouchDevice) return null;

  return (
    <S.Container>
      <S.ToggleBar>
        <S.ToggleButton type="button" onClick={() => setOpen((v) => !v)}>
          {open ? 'Hide iframes' : 'Show iframes'}
        </S.ToggleButton>
        {open && (
          <>
            <S.GameSelectionContainer $isExpanded={isGameSelectionExpanded}>
              <S.GameSelectionLabel
                type="button"
                onClick={() => setIsGameSelectionExpanded((prev) => !prev)}
              >
                게임 선택
              </S.GameSelectionLabel>
              {isGameSelectionExpanded && (
                <S.GameSelectionButtons>
                  {availableGames.map((game) => {
                    const isSelected = gameSequence.includes(game);
                    const order = gameSequence.indexOf(game) + 1; // 선택 순서 (1부터 시작)
                    return (
                      <S.GameSelectionButton
                        key={game}
                        type="button"
                        $selected={isSelected}
                        disabled={isRunning}
                        onClick={() => handleGameToggle(game)}
                      >
                        {MINI_GAME_NAME_MAP[game]}
                        <S.GameOrderBadge $visible={isSelected && order > 0}>
                          {order > 0 ? order : ''}
                        </S.GameOrderBadge>
                      </S.GameSelectionButton>
                    );
                  })}
                </S.GameSelectionButtons>
              )}
            </S.GameSelectionContainer>
            <S.PlayButton type="button" onClick={handleStartTest} disabled={isRunning}>
              {isRunning ? '테스트 실행 중...' : '재생'}
            </S.PlayButton>
            {isRunning && !isPaused && (
              <S.PauseButton type="button" onClick={handlePauseTest}>
                일시 중지
              </S.PauseButton>
            )}
            {isRunning && isPaused && (
              <S.ResumeButton type="button" onClick={handleResumeTest}>
                재개
              </S.ResumeButton>
            )}
            {isRunning && (
              <S.StopButton type="button" onClick={handleStopTest}>
                테스트 중단
              </S.StopButton>
            )}
            <AutoTestLogPanel isIframeOpen={open} />
          </>
        )}
      </S.ToggleBar>
      {open && (
        <S.IframePanel>
          {iframeNames.map((name, index) => {
            const path = iframePaths[name] || '';
            const labelText = path ? `${name} - ${path}` : name;
            // 맨 마지막 iframe만 삭제 가능 (host와 guest1은 항상 삭제 불가)
            const lastIndex = iframeNames.length - 1;
            const isLastIframe = index === lastIndex;
            const canDelete = name !== 'host' && name !== 'guest1' && isLastIframe;

            return (
              <S.IframeWrapper key={name} $height={iframeHeight} $useMinHeight={useMinHeight}>
                <S.IframeLabel>{labelText}</S.IframeLabel>
                {canDelete && (
                  <S.DeleteButton
                    type="button"
                    data-delete-button
                    onClick={(e: MouseEvent<HTMLButtonElement>) => {
                      e.stopPropagation();
                      handleDeleteIframe(name);
                    }}
                    aria-label={`Remove ${name}`}
                  >
                    ×
                  </S.DeleteButton>
                )}
                <S.PreviewIframe
                  ref={(el) => {
                    if (el) {
                      iframeRefs.current[name] = el;
                    }
                  }}
                  name={name}
                  title={`preview-${index === 0 ? 'left' : 'right'}`}
                  src="/"
                />
              </S.IframeWrapper>
            );
          })}
          {canAddMore && (
            <S.IframeWrapper $height={iframeHeight} $useMinHeight={useMinHeight}>
              <S.AddIframeButton type="button" onClick={handleAddIframe}>
                +
              </S.AddIframeButton>
            </S.IframeWrapper>
          )}
        </S.IframePanel>
      )}
    </S.Container>
  );
};

export default IframePreviewToggle;
