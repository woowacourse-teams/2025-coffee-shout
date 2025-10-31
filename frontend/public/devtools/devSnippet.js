/* eslint-env browser */

import { NetworkCollector } from './core/networkCollector.js';
import { setupFetchHook } from './hooks/fetchHook.js';
import { setupWebSocketHook } from './hooks/websocketHook.js';

const w = typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined;
if (!w) {
  // non-browser guard
  throw new Error('dev-snippet.js requires browser environment');
}
if (w.__DEV_SNIPPET__) {
  // Already initialized
  throw new Error('dev-snippet.js already initialized');
}
w.__DEV_SNIPPET__ = true;
w.console && w.console.log('[DEV SNIPPET] active');

// Initialize collector
if (!w.__networkCollector__) {
  try {
    w.__networkCollector__ = new NetworkCollector(1000);
  } catch {
    /* noop */
  }
}

const context = w.self === w.top ? 'MAIN' : w.name || 'IFRAME';

// Setup hooks
setupFetchHook(w, w.__networkCollector__, context);
setupWebSocketHook(w, w.__networkCollector__, context);
