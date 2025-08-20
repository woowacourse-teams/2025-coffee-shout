import React from 'react';
import type { Preview } from '@storybook/react-webpack5';
import { ThemeProvider } from '@emotion/react';
import { theme } from '../src/styles/theme';

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={theme}>
        <Story />
      </ThemeProvider>
    ),
  ],
};

export default preview;
