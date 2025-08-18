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

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <IdentifierProvider>
        <WebSocketProvider>
          <ParticipantsProvider>
            <PlayerTypeProvider>
              <ProbabilityHistoryProvider>
                <ToastProvider>
                  <ModalProvider>
                    <Outlet />
                  </ModalProvider>
                </ToastProvider>
              </ProbabilityHistoryProvider>
            </PlayerTypeProvider>
          </ParticipantsProvider>
        </WebSocketProvider>
      </IdentifierProvider>
    </ThemeProvider>
  );
};

export default App;
