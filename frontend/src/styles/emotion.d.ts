import '@emotion/react';
import { theme } from './theme';

declare module '@emotion/react' {
  export type Theme = {
    color: typeof theme.color;
    typography: typeof theme.typography;
  };
}
