import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { IdentifierProvider } from './contexts/Identifier/IdentifierProvider';
import { theme } from './styles/theme';
import { PlayerTypeProvider } from './contexts/PlayerType/PlayerTypeProvider';
import ProbabilityHistoryProvider from './contexts/ProbabilityHistory/ProbabilityHistoryProvider';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <WebSocketProvider>
        <IdentifierProvider>
          <PlayerTypeProvider>
            <ProbabilityHistoryProvider>
              <ModalProvider>
                <Outlet />
              </ModalProvider>
            </ProbabilityHistoryProvider>
          </PlayerTypeProvider>
        </IdentifierProvider>
      </WebSocketProvider>
    </ThemeProvider>
  );
};

export default App;
