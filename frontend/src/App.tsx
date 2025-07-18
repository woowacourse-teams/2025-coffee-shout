import { ThemeProvider } from '@emotion/react';
import { Outlet } from 'react-router-dom';
import { ModalProvider } from './features/ui/Modal/ModalContext';
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
