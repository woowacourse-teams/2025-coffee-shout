import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { ModalProvider } from './components/@common/Modal/ModalContext';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <ModalProvider>
        <Outlet />
      </ModalProvider>
    </ThemeProvider>
  );
};

export default App;
