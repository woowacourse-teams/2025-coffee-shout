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
      <Suspense fallback={<div>Loading...</div>}>
        <IdentifierProvider>
          <ParticipantsProvider>
            <WebSocketProvider>
              <PlayerTypeProvider>
                <ProbabilityHistoryProvider>
                  <ToastProvider>
                    <ModalProvider>
                      <Outlet />
                    </ModalProvider>
                  </ToastProvider>
                </ProbabilityHistoryProvider>
              </PlayerTypeProvider>
            </WebSocketProvider>
          </ParticipantsProvider>
        </IdentifierProvider>
      </Suspense>
    </ThemeProvider>
  );
};

export default App;
