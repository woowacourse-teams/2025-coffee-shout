/* eslint-env browser */

import { generateRequestId } from '../utils/utils.js';

/**
 * WebSocket API를 후킹하여 네트워크 요청을 수집합니다.
 * 원본 WebSocket 인스턴스를 그대로 반환하여 호출자에 전혀 영향을 주지 않습니다.
 */
export const setupWebSocketHook = (win, collector, context = {}) => {
  win =
    win || (typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined);
  if (!win || !win.WebSocket) {
    return null;
  }

  if (win.__DEV_WS_WRAPPED__) {
    return win.__DEV_WS_WRAPPED__;
  }

  const OriginalWebSocket = win.WebSocket;

  const originalDescriptor = Object.getOwnPropertyDescriptor(win, 'WebSocket') || {
    configurable: true,
    enumerable: false,
    writable: true,
    value: OriginalWebSocket,
  };

  const ProxiedWebSocket = new Proxy(OriginalWebSocket, {
    construct(target, args) {
      const [url, protocols] = args;
      // 원본 WebSocket 인스턴스를 생성
      const ws = new target(url, protocols);

      // 비동기적으로 이벤트 리스너를 추가하여 원본 동작에 영향 없음
      try {
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
            /* noop - 로그 실패는 원본 흐름에 영향 없음 */
          }
        });

        ws.addEventListener('message', (event) => {
          try {
            let data = event.data;

            if (typeof data === 'string') {
              try {
                // JSON 포맷팅 시도
                data = JSON.stringify(JSON.parse(data), null, 2);
              } catch {
                // JSON이 아니면 원본 문자열 유지
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
            /* noop - 로그 실패는 원본 흐름에 영향 없음 */
          }
        });

        ws.addEventListener('error', (event) => {
          try {
            collector.add({
              id: generateRequestId(),
              type: 'websocket',
              context,
              url: String(url),
              timestamp: Date.now(),
              data: '[error]',
              error: String(event.error || event).slice(0, 512),
            });
          } catch {
            /* noop */
          }
        });

        ws.addEventListener('close', (event) => {
          try {
            collector.add({
              id: generateRequestId(),
              type: 'websocket',
              context,
              url: String(url),
              timestamp: Date.now(),
              data: '[close]',
              code: event.code,
              reason: event.reason || null,
            });
          } catch {
            /* noop */
          }
        });
      } catch {
        /* noop - 리스너 추가 실패는 원본 WebSocket 동작에 영향 없음 */
      }

      // 원본 WebSocket 인스턴스를 그대로 반환
      return ws;
    },
  });

  // 프로퍼티 교체
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
