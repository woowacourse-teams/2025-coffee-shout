import { useCallback, useEffect, useRef, useState } from 'react';
import type { RefObject } from 'react';
import { MiniGameType } from '@/types/miniGame/common';
import { TestMessage } from '@/devtools/autoTest/types/testMessage';
import { getAutoTestLogger } from '../utils/autoTestLogger';

type UseIframeTestMessagingParams = {
  isOpen: boolean;
  iframeNames: string[];
  gameSequence: MiniGameType[];
  iframeRefs: RefObject<Record<string, HTMLIFrameElement | null>>;
};

export type UseIframeTestMessagingResult = {
  isRunning: boolean;
  isPaused: boolean;
  guestReadyState: Record<string, boolean>;
  readyState: Record<string, boolean>;
  iframePaths: Record<string, string>;
  handleStartTest: () => void;
  handleStopTest: () => void;
  handlePauseTest: () => void;
  handleResumeTest: () => void;
};

export const useIframeTestMessaging = ({
  isOpen,
  iframeNames,
  gameSequence,
  iframeRefs,
}: UseIframeTestMessagingParams): UseIframeTestMessagingResult => {
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [guestReadyState, setGuestReadyState] = useState<Record<string, boolean>>({});
  const [readyState, setReadyState] = useState<Record<string, boolean>>({});
  const [iframePaths, setIframePaths] = useState<Record<string, string>>({});

  const joinCodeRef = useRef<string | null>(null);
  const pendingStartTest = useRef<boolean>(false);

  const sendMessageToIframe = useCallback(
    (iframeName: string, message: TestMessage) => {
      const iframe = iframeRefs.current[iframeName];
      if (iframe?.contentWindow) {
        iframe.contentWindow.postMessage(message, '*');
      }
    },
    [iframeRefs]
  );

  const handleStartTest = useCallback(() => {
    const logger = getAutoTestLogger();
    logger.addLog({
      message: 'handleStartTest 호출됨',
      context: 'MAIN',
    });

    setIsRunning(true);
    setIsPaused(false);
    joinCodeRef.current = null;
    setGuestReadyState({});
    setReadyState({});
    pendingStartTest.current = true;

    logger.addLog({
      message: 'pendingStartTest를 true로 설정',
      context: 'MAIN',
    });

    iframeNames.forEach((name) => {
      logger.addLog({
        message: `${name}에 RESET_TO_HOME 전송`,
        context: 'MAIN',
      });
      sendMessageToIframe(name, { type: 'RESET_TO_HOME' });
    });
  }, [iframeNames, sendMessageToIframe]);

  const handleStopTest = useCallback(() => {
    setIsRunning(false);
    setIsPaused(false);
    pendingStartTest.current = false;

    iframeNames.forEach((name) => {
      sendMessageToIframe(name, { type: 'STOP_TEST' });
    });
  }, [iframeNames, sendMessageToIframe]);

  const handlePauseTest = useCallback(() => {
    setIsPaused(true);
    iframeNames.forEach((name) => {
      sendMessageToIframe(name, { type: 'PAUSE_TEST' });
    });
  }, [iframeNames, sendMessageToIframe]);

  const handleResumeTest = useCallback(() => {
    setIsPaused(false);
    iframeNames.forEach((name) => {
      sendMessageToIframe(name, { type: 'RESUME_TEST' });
    });
  }, [iframeNames, sendMessageToIframe]);

  useEffect(() => {
    if (!isOpen) return;

    const handleMessage = (event: MessageEvent<TestMessage>) => {
      if (!event.data || typeof event.data !== 'object' || !('type' in event.data)) {
        return;
      }

      const messageData = event.data;

      switch (messageData.type) {
        case 'JOIN_CODE_RECEIVED': {
          const { joinCode } = messageData;
          joinCodeRef.current = joinCode;
          const guestNames = iframeNames.filter((name) => name.startsWith('guest'));

          guestNames.forEach((guestName) => {
            const message: TestMessage = {
              type: 'START_TEST',
              role: 'guest',
              joinCode,
              iframeName: guestName,
              gameSequence,
            };
            sendMessageToIframe(guestName, message);
          });
          break;
        }
        case 'IFRAME_READY': {
          const { iframeName } = messageData;
          const logger = getAutoTestLogger();
          logger.addLog({
            message: 'iframe에서 IFRAME_READY 신호 수신',
            context: 'MAIN',
            data: { iframeName, pendingStartTest: pendingStartTest.current },
          });

          if (iframeName) {
            setReadyState((prev) => ({
              ...prev,
              [iframeName]: true,
            }));
          }

          if (pendingStartTest.current && iframeName === 'host') {
            logger.addLog({
              message: '조건 충족, START_TEST 전송 (IFRAME_READY 수신)',
              context: 'MAIN',
            });

            setTimeout(() => {
              const message: TestMessage = {
                type: 'START_TEST',
                role: 'host',
                gameSequence,
              };
              logger.addLog({
                message: 'IFRAME_READY 후 host에 START_TEST 전송',
                context: 'MAIN',
                data: { gameSequence },
              });
              sendMessageToIframe('host', message);
              pendingStartTest.current = false;
            }, 0);
          }
          break;
        }
        case 'GUEST_READY': {
          const { iframeName } = messageData;
          if (iframeName) {
            setGuestReadyState((prev) => ({
              ...prev,
              [iframeName]: true,
            }));
          }
          break;
        }
        case 'PATH_CHANGE': {
          const { iframeName, path } = messageData;
          setIframePaths((prev) => ({
            ...prev,
            [iframeName]: path,
          }));
          break;
        }
        case 'TEST_COMPLETED': {
          setIsRunning(false);
          setIsPaused(false);
          break;
        }
        default:
          break;
      }
    };

    window.addEventListener('message', handleMessage);
    return () => {
      window.removeEventListener('message', handleMessage);
    };
  }, [gameSequence, iframeNames, isOpen, sendMessageToIframe]);

  useEffect(() => {
    if (!isOpen) return;

    const guestNames = iframeNames.filter((name) => name.startsWith('guest'));
    if (guestNames.length === 0) return;
    const allGuestsReady = guestNames.every((guestName) => guestReadyState[guestName]);

    if (allGuestsReady) {
      sendMessageToIframe('host', { type: 'CLICK_GAME_START' });
    }
  }, [guestReadyState, iframeNames, isOpen, sendMessageToIframe]);

  return {
    isRunning,
    isPaused,
    guestReadyState,
    readyState,
    iframePaths,
    handleStartTest,
    handleStopTest,
    handlePauseTest,
    handleResumeTest,
  };
};
