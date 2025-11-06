/* eslint-env browser */

import { NetworkCollector } from './core/networkCollector.js';
import { setupFetchHook } from './hooks/fetchHook.js';
import { setupWebSocketHook } from './hooks/websocketHook.js';
import { getSafeWindow } from './utils/getSafeWindow.js';

const MAX_REQUESTS = 1000;

const MARKERS = {
  SNIPPET: '__DEV_SNIPPET__',
  COLLECTOR: '__networkCollector__',
};

const w = getSafeWindow();

if (!w) {
  // non-browser guard
  throw new Error('dev-snippet.js requires browser environment');
}

if (w[MARKERS.SNIPPET]) {
  // Already initialized
  throw new Error('dev-snippet.js already initialized');
}

w[MARKERS.SNIPPET] = true;
w.console && w.console.log('[DEV SNIPPET] active');

// Initialize collector
if (!w[MARKERS.COLLECTOR]) {
  try {
    w[MARKERS.COLLECTOR] = new NetworkCollector(MAX_REQUESTS);
  } catch {
    /* noop */
  }
}

const context = w.self === w.top ? 'MAIN' : w.name || 'IFRAME';

// Setup hooks
setupFetchHook(w, w[MARKERS.COLLECTOR], context);
setupWebSocketHook(w, w[MARKERS.COLLECTOR], context);
