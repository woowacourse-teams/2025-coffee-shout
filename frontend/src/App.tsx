import { ThemeProvider } from '@emotion/react';
import { Suspense, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import GlobalErrorBoundary from './components/@common/ErrorBoundary/GlobalErrorBoundary';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { ToastProvider } from './components/@common/Toast/ToastContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { ParticipantsProvider } from './contexts/Participants/ParticipantsProvider';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';
import { theme } from './styles/theme';
import IframePreviewToggle from './devtools/components/IframePreviewToggle/IframePreviewToggle';
import NetworkDebuggerPanel from './devtools/components/NetworkDebuggerPanel/NetworkDebuggerPanel';
import { setupAutoTestListener } from './devtools/autoFlowTest/autoTestFlow';

const App = () => {
  const location = useLocation();

  useEffect(() => {
    const isTopWindow = (() => {
      if (typeof window === 'undefined') return false;
      try {
        return window.self === window.top;
      } catch {
        return false;
      }
    })();

    if (!isTopWindow) {
      const cleanup = setupAutoTestListener();

      // 렌더링 완료 후 ready 신호 전송 (host만)
      const sendReady = () => {
        const iframeName = window.frameElement?.getAttribute('name') || '';
        // host만 READY 신호를 보냄
        if (iframeName === 'host' && window.parent && window.parent !== window) {
          console.log('[AutoTest] Rendering complete, sending READY signal', { iframeName });
          window.parent.postMessage({ type: 'READY', iframeName }, '*');
        }
      };

      // DOM 렌더링이 완료된 후 ready 신호 전송
      setTimeout(sendReady, 100);

      return cleanup;
    }
  }, []);

  useEffect(() => {
    const isTopWindow = (() => {
      if (typeof window === 'undefined') return false;
      try {
        return window.self === window.top;
      } catch {
        return false;
      }
    })();

    if (!isTopWindow && window.parent && window.parent !== window) {
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
    <ThemeProvider theme={theme}>
      <IframePreviewToggle />
      <NetworkDebuggerPanel />
      <IdentifierProvider>
        <ParticipantsProvider>
          <WebSocketProvider>
            <PlayerTypeProvider>
              <ProbabilityHistoryProvider>
                <GlobalErrorBoundary>
                  <ToastProvider>
                    <ModalProvider>
                      <Suspense fallback={<div>Loading...</div>}>
                        <Outlet />
                      </Suspense>
                    </ModalProvider>
                  </ToastProvider>
                </GlobalErrorBoundary>
              </ProbabilityHistoryProvider>
            </PlayerTypeProvider>
          </WebSocketProvider>
        </ParticipantsProvider>
      </IdentifierProvider>
    </ThemeProvider>
  );
};

export default App;
