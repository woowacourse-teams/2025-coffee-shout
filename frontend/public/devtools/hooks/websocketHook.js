/* eslint-env browser */

import { generateRequestId } from '../utils/utils.js';

/**
 * WebSocket API를 후킹하여 네트워크 요청을 수집합니다.
 */
export const setupWebSocketHook = (window, collector, context) => {
  const OriginalWebSocket = window.WebSocket;
  if (!OriginalWebSocket || window.__DEV_WS_WRAPPED__) {
    return;
  }

  window.__DEV_WS_WRAPPED__ = true;
  window.WebSocket = new Proxy(OriginalWebSocket, {
    construct(target, args) {
      const [url, protocols] = args;
      const ws = new target(url, protocols);

      try {
        ws.addEventListener('open', () => {
          collector.add({
            id: generateRequestId(),
            type: 'websocket',
            context,
            url: String(url),
            timestamp: Date.now(),
            data: '[open]',
          });
        });

        ws.addEventListener('message', (event) => {
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
        });
      } catch {
        /* noop */
      }

      return ws;
    },
  });
};
