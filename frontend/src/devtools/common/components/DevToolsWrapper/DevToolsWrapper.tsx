import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import IframePreviewToggle from '../../../autoTest/components/IframePreviewToggle/IframePreviewToggle';

import { setupAutoTestListener } from '../../../autoTest/flow/autoTestFlow';
import {
  initializeAutoTestLogger,
  getAutoTestLogger,
} from '../../../autoTest/utils/autoTestLogger';
import NetworkDebuggerPanel from '@/devtools/networkDebug/components/NetworkDebuggerPanel/NetworkDebuggerPanel';

const isTopWindow = (): boolean => {
  if (typeof window === 'undefined') return false;
  try {
    return window.self === window.top;
  } catch {
    return false;
  }
};

export const DevToolsWrapper = () => {
  const location = useLocation();

  // AutoTestLogger 초기화
  useEffect(() => {
    if (process.env.ENABLE_DEVTOOLS) {
      initializeAutoTestLogger();
    }
  }, []);

  // Auto test listener 설정
  useEffect(() => {
    if (!isTopWindow()) {
      const cleanup = setupAutoTestListener();

      const sendReady = () => {
        const iframeName = window.frameElement?.getAttribute('name') || '';
        if (iframeName === 'host' && window.parent && window.parent !== window) {
          const logger = getAutoTestLogger();
          logger.addLog({
            message: '렌더링 완료, READY 신호 전송',
            context: iframeName,
            data: { iframeName },
          });
          window.parent.postMessage({ type: 'READY', iframeName }, '*');
        }
      };

      setTimeout(sendReady, 100);
      return cleanup;
    }
  }, []);

  // Path change 감지 및 메시지 전송
  useEffect(() => {
    if (!isTopWindow() && window.parent && window.parent !== window) {
      const iframeName = window.frameElement?.getAttribute('name') || '';
      if (iframeName) {
        window.parent.postMessage(
          {
            type: 'PATH_CHANGE',
            iframeName,
            path: location.pathname,
          },
          '*'
        );

        const orderPagePattern = /^\/room\/[^/]+\/order$/;
        if (orderPagePattern.test(location.pathname)) {
          window.parent.postMessage({ type: 'TEST_COMPLETED' }, '*');
        }
      }
    }
  }, [location.pathname]);

  return (
    <>
      <IframePreviewToggle />
      <NetworkDebuggerPanel />
    </>
  );
};
