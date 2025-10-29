import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import './styles/global.css';
import './styles/reset.css';
import * as Sentry from '@sentry/react';
import { injectSnippet } from './devtools/injectSnippetToIframes';

if (process.env.NODE_ENV === 'production') {
  Sentry.init({
    dsn: process.env.DSN_KEY || '',
    release: process.env.VERSION || '1.0.0',
    environment: process.env.NODE_ENV || 'development',
    sendDefaultPii: true,
    integrations: [Sentry.browserTracingIntegration(), Sentry.replayIntegration()],
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    tracesSampleRate: 0.2,
    tracePropagationTargets: ['localhost:8080', process.env.API_URL || '', /^\/api\//],
  });
}

// 개발 모드에서 디버거 초기화 (React 렌더링과 완전 분리)
if (process.env.NODE_ENV === 'development') {
  const initDebugger = () => {
    injectSnippet();
  };

  // window load 완료 후 실행
  if (document.readyState === 'complete') {
    initDebugger();
  } else {
    window.addEventListener('load', initDebugger);
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
