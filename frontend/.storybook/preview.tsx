import React from 'react';
import { ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { ModalProvider } from '../src/components/@common/Modal/ModalContext';
import { IdentifierProvider } from '../src/contexts/Identifier/IdentifierProvider';
import CardGameProvider from '../src/contexts/CardGame/CardGameProvider';
import { WebSocketProvider } from '../src/apis/websocket/contexts/WebSocketProvider';
import '../src/styles/global.css';
import '../src/styles/reset.css';
import { theme } from '../src/styles/theme';
import { MemoryRouter } from 'react-router-dom';

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={theme}>
        <WebSocketProvider>
          <MemoryRouter>
            <IdentifierProvider>
              <CardGameProvider>
                <ModalProvider>
                  <Story />
                </ModalProvider>
              </CardGameProvider>
            </IdentifierProvider>
          </MemoryRouter>
        </WebSocketProvider>
      </ThemeProvider>
    ),
  ],
};

export default preview;
