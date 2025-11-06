import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import IframePreviewToggle from '../IframePreviewToggle/IframePreviewToggle';
import NetworkDebuggerPanel from '../NetworkDebuggerPanel/NetworkDebuggerPanel';
import { setupAutoTestListener } from '../../autoFlowTest/autoTestFlow';

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

  // Auto test listener 설정
  useEffect(() => {
    if (!isTopWindow()) {
      const cleanup = setupAutoTestListener();

      const sendReady = () => {
        const iframeName = window.frameElement?.getAttribute('name') || '';
        if (iframeName === 'host' && window.parent && window.parent !== window) {
          console.log('[AutoTest] Rendering complete, sending READY signal', { iframeName });
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

