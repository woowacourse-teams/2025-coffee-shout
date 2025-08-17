import { ToastProvider } from '@/components/@common/Toast/ToastContext';
import { ThemeProvider } from '@emotion/react';
import type { Preview } from '@storybook/react-webpack5';
import { MemoryRouter } from 'react-router-dom';
import { WebSocketProvider } from '../src/apis/websocket/contexts/WebSocketProvider';
import { ModalProvider } from '../src/components/@common/Modal/ModalContext';
import CardGameProvider from '../src/contexts/CardGame/CardGameProvider';
import { IdentifierProvider } from '../src/contexts/Identifier/IdentifierProvider';
import '../src/styles/global.css';
import '../src/styles/reset.css';
import { theme } from '../src/styles/theme';

const preview: Preview = {
  decorators: [
    (Story) => (
      <ThemeProvider theme={theme}>
        <IdentifierProvider>
          <WebSocketProvider>
            <MemoryRouter>
              <CardGameProvider>
                <ToastProvider>
                  <ModalProvider>
                    <Story />
                  </ModalProvider>
                </ToastProvider>
              </CardGameProvider>
            </MemoryRouter>
          </WebSocketProvider>
        </IdentifierProvider>
      </ThemeProvider>
    ),
  ],
};

export default preview;
