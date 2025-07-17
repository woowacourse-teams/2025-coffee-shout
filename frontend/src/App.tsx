import { ThemeProvider } from '@emotion/react';
import { theme } from './styles/theme';
import ToggleButton from './components/@common/ToggleButton/ToggleButton';

const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <div className="App">
        <h1>Hello, World!</h1>
        <ToggleButton />
      </div>
    </ThemeProvider>
  );
};

export default App;
