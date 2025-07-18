import { ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { ModalProvider } from '../src/features/ui/Modal/ModalContext';
import '../src/styles/global.css';
import '../src/styles/reset.css';
import { theme } from '../src/styles/theme';
import '../src/styles/reset.css';
import '../src/styles/global.css';

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={theme}>
        <ModalProvider>
          <Story />
        </ModalProvider>
      </ThemeProvider>
    ),
  ],
};

export default preview;
