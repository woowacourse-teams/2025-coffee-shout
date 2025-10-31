/* eslint-env browser */

import { generateRequestId } from '../utils/utils.js';

export const setupWebSocketHook = (win, collector, context = {}) => {
  win =
    win || (typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined);
  if (!win?.WebSocket || win.__DEV_WS_WRAPPED__) {
    return win?.__DEV_WS_WRAPPED__ || null;
  }

  const OriginalWebSocket = win.WebSocket;
  const originalDescriptor = Object.getOwnPropertyDescriptor(win, 'WebSocket') || {
    configurable: true,
    enumerable: false,
    writable: true,
    value: OriginalWebSocket,
  };

  const addEventListeners = (ws, url, collector, context) => {
    ws.addEventListener('open', () => {
      try {
        collector.add({
          id: generateRequestId(),
          type: 'websocket',
          context,
          url: String(url),
          timestamp: Date.now(),
          data: '[open]',
        });
      } catch {
        /* noop */
      }
    });

    ws.addEventListener('message', (event) => {
      try {
        let data = event.data;
        if (typeof data === 'string') {
          try {
            data = JSON.stringify(JSON.parse(data), null, 2);
          } catch {
            /* noop */
          }
        } else {
          data = '[Binary data]';
        }

        collector.add({
          id: generateRequestId(),
          type: 'websocket',
          context,
          url: String(url),
          data,
          timestamp: Date.now(),
        });
      } catch {
        /* noop */
      }
    });

    ws.addEventListener('error', () => {
      try {
        collector.add({
          id: generateRequestId(),
          type: 'websocket',
          context,
          url: String(url),
          timestamp: Date.now(),
          data: '[error]',
        });
      } catch {
        /* noop */
      }
    });

    ws.addEventListener('close', () => {
      try {
        collector.add({
          id: generateRequestId(),
          type: 'websocket',
          context,
          url: String(url),
          timestamp: Date.now(),
          data: '[close]',
        });
      } catch {
        /* noop */
      }
    });
  };

  const ProxiedWebSocket = new Proxy(OriginalWebSocket, {
    construct(target, args) {
      const [url] = args;
      const ws = new target(...args);

      try {
        addEventListeners(ws, url, collector, context);
      } catch {
        /* noop */
      }

      return ws;
    },
  });

  Object.defineProperty(win, 'WebSocket', {
    configurable: true,
    enumerable: originalDescriptor.enumerable,
    writable: originalDescriptor.writable,
    value: ProxiedWebSocket,
  });

  const handle = {
    restore() {
      try {
        Object.defineProperty(win, 'WebSocket', originalDescriptor);
        delete win.__DEV_WS_WRAPPED__;
      } catch {
        /* noop */
      }
    },
  };

  win.__DEV_WS_WRAPPED__ = handle;
  return handle;
};
