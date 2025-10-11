import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { ToastProvider } from './components/@common/Toast/ToastContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { ParticipantsProvider } from './contexts/Participants/ParticipantsProvider';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';
import { theme } from './styles/theme';
import { Suspense } from 'react';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <IdentifierProvider>
        <ParticipantsProvider>
          <WebSocketProvider>
            <PlayerTypeProvider>
              <ProbabilityHistoryProvider>
                <ToastProvider>
                  <ModalProvider>
                    <Suspense fallback={<div>Loading...</div>}>
                      <Outlet />
                    </Suspense>
                  </ModalProvider>
                </ToastProvider>
              </ProbabilityHistoryProvider>
            </PlayerTypeProvider>
          </WebSocketProvider>
        </ParticipantsProvider>
      </IdentifierProvider>
    </ThemeProvider>
  );
};

export default App;
