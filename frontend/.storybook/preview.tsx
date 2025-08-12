import React from 'react';
import { ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { ModalProvider } from '../src/components/@common/Modal/ModalContext';
import { IdentifierProvider } from '../src/contexts/Identifier/IdentifierProvider';
import '../src/styles/global.css';
import '../src/styles/reset.css';
import { theme } from '../src/styles/theme';
import { MemoryRouter } from 'react-router-dom';

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={theme}>
        <MemoryRouter>
          <IdentifierProvider>
            <ModalProvider>
              <Story />
            </ModalProvider>
          </IdentifierProvider>
        </MemoryRouter>
      </ThemeProvider>
    ),
  ],
};

export default preview;
