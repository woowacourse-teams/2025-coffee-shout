import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import './styles/global.css';
import './styles/reset.css';
import * as Sentry from '@sentry/react';

if (process.env.NODE_ENV === 'production' && process.env.REACT_APP_DSN_KEY) {
  Sentry.init({
    dsn: process.env.REACT_APP_DSN_KEY,
    release: process.env.REACT_APP_VERSION,
    environment: process.env.NODE_ENV,
    sendDefaultPii: true,
    integrations: [Sentry.browserTracingIntegration(), Sentry.replayIntegration()],
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    tracesSampleRate: 0.2,
    tracePropagationTargets: ['localhost:8080', 'https://api.coffee-shout.com', /^\/api\//],
  });
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
