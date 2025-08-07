import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import './styles/global.css';
import './styles/reset.css';
import * as Sentry from '@sentry/react';

// if (process.env.NODE_ENV === 'production') {
Sentry.init({
  dsn: 'https://b2afc6b0a990c07e81f7458240917d0f@o4509780188987392.ingest.us.sentry.io/4509784133271552',
  release: process.env.REACT_APP_VERSION || '1.0.0',
  // environment: process.env.NODE_ENV || 'development'
  sendDefaultPii: true,
  integrations: [Sentry.browserTracingIntegration(), Sentry.replayIntegration()],
  replaysSessionSampleRate: 0.1,
  replaysOnErrorSampleRate: 1.0,
  tracesSampleRate: 0.2,
  tracePropagationTargets: ['localhost:8080', 'https://api.dev.coffee-shout.com', /^\/api\//],
});
// }

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
