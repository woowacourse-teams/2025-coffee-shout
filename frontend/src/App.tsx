import { ThemeProvider } from '@emotion/react';
import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import GlobalErrorBoundary from './components/@common/ErrorBoundary/GlobalErrorBoundary';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { ToastProvider } from './components/@common/Toast/ToastContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { ParticipantsProvider } from './contexts/Participants/ParticipantsProvider';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';
import { theme } from './styles/theme';
import IframePreviewToggle from './devtools/components/IframePreviewToggle';
import NetworkDebuggerPanel from './devtools/components/NetworkDebuggerPanel/NetworkDebuggerPanel';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <IdentifierProvider>
        <ParticipantsProvider>
          <WebSocketProvider>
            <PlayerTypeProvider>
              <ProbabilityHistoryProvider>
                <GlobalErrorBoundary>
                  <ToastProvider>
                    <ModalProvider>
                      <IframePreviewToggle />
                      <NetworkDebuggerPanel />
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
