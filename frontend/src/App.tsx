import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { theme } from './styles/theme';
import { PlayerRoleProvider } from './contexts/PlayerRole/PlayerRoleProvider';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <WebSocketProvider>
        <PlayerRoleProvider>
          <ModalProvider>
            <Outlet />
          </ModalProvider>
        </PlayerRoleProvider>
      </WebSocketProvider>
    </ThemeProvider>
  );
};

export default App;
