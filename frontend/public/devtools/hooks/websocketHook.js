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

  // WebSocket 연결 추적을 위한 맵 (URL+컨텍스트를 키로 사용)
  const wsConnectionMap = new Map();

  const addEventListeners = (ws, url, collector, context) => {
    const connectionKey = `${context}:${url}`;
    const connectionId = generateRequestId();
    const messages = [];
    let connectionStartTime = Date.now();

    ws.addEventListener('open', () => {
      try {
        connectionStartTime = Date.now();
        const request = {
          id: connectionId,
          type: 'websocket',
          context,
          url: String(url),
          status: 101,
          timestamp: connectionStartTime,
          messages: messages,
          connectionStatus: 'open',
        };
        // messages 배열을 직접 참조로 공유하여 업데이트가 반영되도록 함
        wsConnectionMap.set(connectionKey, request);
        collector.add(request);
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

        const message = {
          type: 'received',
          data,
          timestamp: Date.now(),
        };
        // messages 배열에 직접 추가 (객체 참조가 공유되므로 collector에도 반영됨)
        messages.push(message);
      } catch {
        /* noop */
      }
    });

    ws.addEventListener('error', () => {
      try {
        const connection = wsConnectionMap.get(connectionKey);
        if (connection) {
          connection.connectionStatus = 'error';
        }
      } catch {
        /* noop */
      }
    });

    ws.addEventListener('close', () => {
      try {
        const connection = wsConnectionMap.get(connectionKey);
        if (connection) {
          connection.connectionStatus = 'closed';
          const duration = Date.now() - connectionStartTime;
          connection.durationMs = duration;
        }
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
