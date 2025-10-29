import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import router from './router';
import './styles/global.css';
import './styles/reset.css';
import * as Sentry from '@sentry/react';
import { injectSnippetToIframes } from './utils/injectScriptToIframes';

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

// 개발 모드에서만 iframe 스크립트 자동 주입 활성화
if (process.env.NODE_ENV === 'development') {
  // DOM이 준비된 후 실행
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', injectSnippetToIframes);
  } else {
    // 이미 로드된 경우 즉시 실행
    injectSnippetToIframes();
  }
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
