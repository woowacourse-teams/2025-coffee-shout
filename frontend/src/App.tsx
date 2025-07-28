import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { theme } from './styles/theme';
import { UserRoleProvider } from './contexts/UserRoleProvider';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <UserRoleProvider>
        <ModalProvider>
          <Outlet />
        </ModalProvider>
      </UserRoleProvider>
    </ThemeProvider>
  );
};

export default App;
