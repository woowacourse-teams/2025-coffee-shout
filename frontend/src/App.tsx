import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { theme } from './styles/theme';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';
import { ParticipantsProvider } from './contexts/Participants/ParticipantsProvider';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <IdentifierProvider>
        <WebSocketProvider>
          <ParticipantsProvider>
            <PlayerTypeProvider>
              <ProbabilityHistoryProvider>
                <ModalProvider>
                  <Outlet />
                </ModalProvider>
              </ProbabilityHistoryProvider>
            </PlayerTypeProvider>
          </ParticipantsProvider>
        </WebSocketProvider>
      </IdentifierProvider>
    </ThemeProvider>
  );
};

export default App;
