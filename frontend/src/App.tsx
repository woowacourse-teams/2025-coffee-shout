import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { theme } from './styles/theme';
import { PlayerRoleProvider } from './contexts/PlayerRoleProvider';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <PlayerRoleProvider>
        <ModalProvider>
          <Outlet />
        </ModalProvider>
      </PlayerRoleProvider>
    </ThemeProvider>
  );
};

export default App;
