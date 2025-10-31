/* eslint-env browser */

import { extractRequestInfo, generateRequestId } from '../utils/utils.js';

export const setupFetchHook = (win, collector, context = {}) => {
  win =
    win || (typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined);
  if (!win?.fetch || win.__DEV_FETCH_WRAPPED__) {
    return win?.__DEV_FETCH_WRAPPED__ || null;
  }

  const originalFetch = win.fetch;
  const originalDescriptor = Object.getOwnPropertyDescriptor(win, 'fetch') || {
    configurable: true,
    enumerable: false,
    writable: true,
    value: originalFetch,
  };

  const wrapper = function (input, init = {}) {
    const startedAt = Date.now();
    const { method, url } = extractRequestInfo(input, init) || {
      method: 'GET',
      url: String(input),
    };

    const originalPromise = Reflect.apply(originalFetch, this, [input, init]);

    originalPromise
      .then(async (res) => {
        try {
          let responseBody = null;
          const ct = res?.headers?.get?.('content-type') || '';

          if (res?.clone && (ct.includes('application/json') || ct.startsWith('text/'))) {
            try {
              responseBody = (await res.clone().text()).slice(0, 2048);
            } catch {
              /* noop */
            }
          }

          collector.add({
            id: generateRequestId(),
            type: 'fetch',
            context,
            method,
            url,
            status: res.status,
            timestamp: Date.now(),
            responseBody,
            durationMs: Date.now() - startedAt,
          });
        } catch {
          /* noop */
        }
        return res;
      })
      .catch((err) => {
        try {
          collector.add({
            id: generateRequestId(),
            type: 'fetch',
            context,
            method,
            url,
            status: 'NETWORK_ERROR',
            timestamp: Date.now(),
            durationMs: Date.now() - startedAt,
            errorMessage: String(err).slice(0, 512),
          });
        } catch {
          /* noop */
        }
      });

    return originalPromise;
  };

  Object.defineProperty(win, 'fetch', {
    configurable: true,
    enumerable: originalDescriptor.enumerable,
    writable: originalDescriptor.writable,
    value: wrapper,
  });

  const handle = {
    restore() {
      try {
        Object.defineProperty(win, 'fetch', originalDescriptor);
        delete win.__DEV_FETCH_WRAPPED__;
      } catch {
        /* noop */
      }
    },
  };

  win.__DEV_FETCH_WRAPPED__ = handle;
  return handle;
};
