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

    // STOMP MESSAGE 파싱 함수
    const parseStompMessage = (rawData) => {
      try {
        if (typeof rawData !== 'string') return null;

        // STOMP over WebSocket 형식 확인 (예: a["MESSAGE\n..."] 또는 a['MESSAGE\n...'])
        let stompFrame = null;

        // a["..."] 형식
        const doubleQuotePattern = /^a\["((?:[^"\\]|\\.)*)"\]$/;
        // a['...'] 형식
        const singleQuotePattern = /^a\['((?:[^'\\]|\\.)*)'\]$/;

        let match = rawData.match(doubleQuotePattern);
        if (match) {
          // 이스케이프 문자 처리
          stompFrame = match[1].replace(/\\(.)/g, (match, char) => {
            if (char === 'n') return '\n';
            if (char === 'r') return '\r';
            if (char === 't') return '\t';
            if (char === '\\') return '\\';
            if (char === '"') return '"';
            if (char === "'") return "'";
            return char;
          });
        } else {
          match = rawData.match(singleQuotePattern);
          if (match) {
            stompFrame = match[1].replace(/\\(.)/g, (match, char) => {
              if (char === 'n') return '\n';
              if (char === 'r') return '\r';
              if (char === 't') return '\t';
              if (char === '\\') return '\\';
              if (char === "'") return "'";
              if (char === '"') return '"';
              return char;
            });
          }
        }

        if (!stompFrame) return null;

        // 헤더와 본문 분리 (빈 줄로 구분)
        const headerBodySplit = stompFrame.indexOf('\n\n');
        if (headerBodySplit === -1) return null;

        const headersPart = stompFrame.substring(0, headerBodySplit);
        let bodyPart = stompFrame.substring(headerBodySplit + 2);

        // 끝의 null 문자 제거 (STOMP 프레임 종료 문자)
        if (bodyPart.length > 0 && bodyPart.charCodeAt(bodyPart.length - 1) === 0) {
          bodyPart = bodyPart.substring(0, bodyPart.length - 1);
        }

        // 헤더 파싱
        const headerLines = headersPart.split('\n');
        if (headerLines.length === 0) return null;

        const command = headerLines[0].trim();

        // MESSAGE가 아니면 파싱하지 않음
        if (command !== 'MESSAGE') return null;

        const headers = {};
        for (let i = 1; i < headerLines.length; i++) {
          const line = headerLines[i].trim();
          if (!line) continue;
          const colonIndex = line.indexOf(':');
          if (colonIndex > 0) {
            const key = line.substring(0, colonIndex).trim();
            const value = line.substring(colonIndex + 1).trim();
            headers[key] = value;
          }
        }

        // 본문 포맷팅 (JSON인 경우)
        let formattedBody = bodyPart;
        if (headers['content-type'] === 'application/json' && bodyPart) {
          try {
            const parsed = JSON.parse(bodyPart);
            formattedBody = JSON.stringify(parsed, null, 2);
          } catch {
            // JSON 파싱 실패 시 원본 유지
          }
        }

        return {
          headers,
          body: formattedBody,
          rawData,
        };
      } catch {
        // 디버깅용 (개발 환경에서만)
        // console.warn('STOMP 파싱 실패:', error, rawData);
        return null;
      }
    };

    ws.addEventListener('message', (event) => {
      try {
        let data = event.data;
        let isStompMessage = false;
        let stompHeaders = undefined;
        let stompBody = undefined;

        // STOMP MESSAGE 파싱 시도
        const stompParsed = parseStompMessage(event.data);
        if (stompParsed) {
          isStompMessage = true;
          stompHeaders = stompParsed.headers;
          stompBody = stompParsed.body;
          data = stompParsed.body; // 표시용 데이터는 포맷팅된 본문
        } else if (typeof data === 'string') {
          // 일반 JSON 파싱 시도
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
          isStompMessage,
          stompHeaders,
          stompBody,
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
