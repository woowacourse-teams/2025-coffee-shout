import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { RefObject } from 'react';
import { MiniGameType } from '@/types/miniGame/common';
import { TestMessage } from '@/devtools/autoTest/types/testMessage';
import { createIframeMessenger } from '../utils/iframeMessenger';

type MessageHandler = (message: TestMessage) => void;
type MessageHandlers = Partial<Record<TestMessage['type'], MessageHandler>>;

type UseIframeTestPostMessageParams = {
  isOpen: boolean;
  iframeNames: string[];
  gameSequence: MiniGameType[];
  iframeRefs: RefObject<Record<string, HTMLIFrameElement | null>>;
};

export type UseIframeTestPostMessageResult = {
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

export const useIframeTestPostMessage = ({
  isOpen,
  iframeNames,
  gameSequence,
  iframeRefs,
}: UseIframeTestPostMessageParams): UseIframeTestPostMessageResult => {
  // 상태
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [isPaused, setIsPaused] = useState<boolean>(false);
  const [guestReadyState, setGuestReadyState] = useState<Record<string, boolean>>({});
  const [readyState, setReadyState] = useState<Record<string, boolean>>({});
  const [iframePaths, setIframePaths] = useState<Record<string, string>>({});

  // 레퍼런스
  const joinCodeRef = useRef<string | null>(null);
  const pendingStartTest = useRef<boolean>(false);

  // 파생 값
  const guestIframeNames = useMemo(
    () => iframeNames.filter((name) => name.startsWith('guest')),
    [iframeNames]
  );

  const { sendToIframe, broadcastToIframes } = useMemo(() => {
    return createIframeMessenger({
      iframeRefs,
    });
  }, [iframeRefs]);

  // 명령 핸들러
  const handleStartTest = useCallback(() => {
    setIsRunning(true);
    setIsPaused(false);
    joinCodeRef.current = null;
    setGuestReadyState({});
    setReadyState({});
    pendingStartTest.current = true;

    broadcastToIframes(iframeNames, () => ({ type: 'RESET_TO_HOME' }));
  }, [broadcastToIframes, iframeNames]);

  const handleStopTest = useCallback(() => {
    setIsRunning(false);
    setIsPaused(false);
    pendingStartTest.current = false;

    broadcastToIframes(iframeNames, () => ({ type: 'STOP_TEST' }));
  }, [broadcastToIframes, iframeNames]);

  const handlePauseTest = useCallback(() => {
    setIsPaused(true);
    broadcastToIframes(iframeNames, () => ({ type: 'PAUSE_TEST' }));
  }, [broadcastToIframes, iframeNames]);

  const handleResumeTest = useCallback(() => {
    setIsPaused(false);
    broadcastToIframes(iframeNames, () => ({ type: 'RESUME_TEST' }));
  }, [broadcastToIframes, iframeNames]);

  // 메시지 처리
  useEffect(() => {
    if (!isOpen) return;

    const messageHandlers: MessageHandlers = {
      JOIN_CODE_RECEIVED: (message) => {
        if (message.type !== 'JOIN_CODE_RECEIVED') return;

        const { joinCode } = message;
        joinCodeRef.current = joinCode;

        broadcastToIframes(guestIframeNames, (guestName) => ({
          type: 'START_TEST',
          role: 'guest',
          joinCode,
          iframeName: guestName,
          gameSequence,
        }));
      },
      IFRAME_READY: (message) => {
        if (message.type !== 'IFRAME_READY') return;

        const { iframeName } = message;

        if (iframeName) {
          setReadyState((prev) => ({
            ...prev,
            [iframeName]: true,
          }));
        }

        if (pendingStartTest.current && iframeName === 'host') {
          setTimeout(() => {
            const startMessage: TestMessage = {
              type: 'START_TEST',
              role: 'host',
              gameSequence,
            };
            sendToIframe('host', startMessage);
            pendingStartTest.current = false;
          }, 0);
        }
      },
      GUEST_READY: (message) => {
        if (message.type !== 'GUEST_READY') return;

        const { iframeName } = message;
        if (iframeName) {
          setGuestReadyState((prev) => ({
            ...prev,
            [iframeName]: true,
          }));
        }
      },
      PATH_CHANGE: (message) => {
        if (message.type !== 'PATH_CHANGE') return;

        const { iframeName, path } = message;
        setIframePaths((prev) => ({
          ...prev,
          [iframeName]: path,
        }));
      },
      TEST_COMPLETED: () => {
        setIsRunning(false);
        setIsPaused(false);
      },
    };

    const handleMessage = (event: MessageEvent<TestMessage>) => {
      if (!event.data || typeof event.data !== 'object' || !('type' in event.data)) {
        return;
      }

      const messageData = event.data;
      const handler = messageHandlers[messageData.type];
      if (handler) {
        handler(messageData as never);
      }
    };

    window.addEventListener('message', handleMessage);
    return () => {
      window.removeEventListener('message', handleMessage);
    };
  }, [broadcastToIframes, gameSequence, guestIframeNames, isOpen, sendToIframe]);

  // 게스트 준비 상태 감시
  useEffect(() => {
    if (!isOpen) return;
    if (guestIframeNames.length === 0) return;

    const allGuestsReady = guestIframeNames.every((guestName) => guestReadyState[guestName]);
    if (allGuestsReady) {
      sendToIframe('host', { type: 'CLICK_GAME_START' });
    }
  }, [guestIframeNames, guestReadyState, isOpen, sendToIframe]);

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
