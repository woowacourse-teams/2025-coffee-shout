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
