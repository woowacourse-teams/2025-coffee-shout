import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { WebSocketProvider } from './apis/websocket/contexts/WebSocketProvider';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { JoinCodeProvider } from './contexts/JoinCode/JoinCodeProvider';
import { PlayerRoleProvider } from './contexts/PlayerRole/PlayerRoleProvider';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <WebSocketProvider>
        <JoinCodeProvider>
          <PlayerRoleProvider>
            <ModalProvider>
              <Outlet />
            </ModalProvider>
          </PlayerRoleProvider>
        </JoinCodeProvider>
      </WebSocketProvider>
    </ThemeProvider>
  );
};

export default App;
