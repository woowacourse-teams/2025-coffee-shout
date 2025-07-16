import { ThemeProvider } from '@emotion/react';
import { theme } from './styles/theme';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <div className="App">
        <h1>Hello, World!</h1>
      </div>
    </ThemeProvider>
  );
};

export default App;
