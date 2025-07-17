import { useState } from 'react';
import { ThemeProvider } from '@emotion/react';
import ToggleButton from './components/@common/ToggleButton/ToggleButton';
import { theme } from './styles/theme';

const App = () => {
  const [selectedOption, setSelectedOption] = useState('참가자');
  const handleClickOption = (option: string) => {
    setSelectedOption(option);
  };

  return (
    <ThemeProvider theme={theme}>
      <div className="App">
        <h1>Hello, World!</h1>
        <ToggleButton
          options={['참가자', '룰렛', '미니게임']}
          selectedOption={selectedOption}
          onSelectOption={handleClickOption}
        />
      </div>
    </ThemeProvider>
  );
};

export default App;
