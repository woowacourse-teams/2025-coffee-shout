import { ThemeProvider } from '@emotion/react';
import { ModalProvider } from './features/ui/Modal/ModalContext';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <ModalProvider>
        <h1>Hello, World!</h1>
      </ModalProvider>
    </ThemeProvider>
  );
};

export default App;
