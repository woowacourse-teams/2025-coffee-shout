/* eslint-env browser */

import { NetworkCollector } from './core/networkCollector.js';
import { setupFetchHook } from './hooks/fetchHook.js';
import { setupWebSocketHook } from './hooks/websocketHook.js';
import { getSafeWindow } from './utils/common/getSafeWindow.js';

const MAX_REQUESTS = 1000;

const MARKERS = {
  SNIPPET: '__DEV_SNIPPET__',
  COLLECTOR: '__networkCollector__',
};

const w = getSafeWindow();

validateAndInitialize(w);

const collector = initializeCollector(w);
const context = w.self === w.top ? 'MAIN' : w.name || 'IFRAME';

// Setup hooks
setupFetchHook(w, collector, context);
setupWebSocketHook(w, collector, context);

const initializeCollector = (win) => {
  if (!win[MARKERS.COLLECTOR]) {
    try {
      win[MARKERS.COLLECTOR] = new NetworkCollector(MAX_REQUESTS);
    } catch {
      /* noop */
    }
  }
  return win[MARKERS.COLLECTOR];
};

const validateAndInitialize = (win) => {
  if (!win) {
    // non-browser guard
    throw new Error('dev-snippet.js requires browser environment');
  }

  if (win[MARKERS.SNIPPET]) {
    // Already initialized
    throw new Error('dev-snippet.js already initialized');
  }

  win[MARKERS.SNIPPET] = true;
  win.console && win.console.log('[DEV SNIPPET] active');
};
