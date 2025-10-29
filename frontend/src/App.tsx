import { ThemeProvider } from '@emotion/react';
import { Suspense, useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import GlobalErrorBoundary from './components/@common/ErrorBoundary/GlobalErrorBoundary';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { ToastProvider } from './components/@common/Toast/ToastContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { ParticipantsProvider } from './contexts/Participants/ParticipantsProvider';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';
import { injectSnippet } from './devtools/injectSnippetToIframes';
import { theme } from './styles/theme';

const App = () => {
  const [showIframes, setShowIframes] = useState(false);
  const isInIframe = window.self !== window.top;

  // 개발 모드에서 snippet 주입
  useEffect(() => {
    if (process.env.NODE_ENV === 'development') {
      injectSnippet();
    }
  }, []);

  return (
    <>
      {!isInIframe && (
        <button
          onClick={() => setShowIframes(!showIframes)}
          style={{ position: 'fixed', top: 10, left: 10, zIndex: 9999, padding: '8px 16px' }}
        >
          Toggle Iframes
        </button>
      )}

      {showIframes ? (
        <div
          style={{ position: 'fixed', top: 50, left: 10, display: 'flex', gap: 10, zIndex: 9999 }}
        >
          <iframe
            src="/?user=host"
            title="View 1"
            style={{ width: 320, height: '100dvh', border: '1px solid #ccc' }}
            name="host frame"
          />
          <iframe
            src="/?user=guest1"
            title="View 2"
            style={{ width: 320, height: '100dvh', border: '1px solid #ccc' }}
            name="guest1 frame"
          />
        </div>
      ) : (
        <ThemeProvider theme={theme}>
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
      )}
    </>
  );
};

export default App;
