/* eslint-env browser */
/* global console */

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

    // STOMP 메시지 파싱 함수 (모든 STOMP 명령어 지원)
    const parseStompMessage = (rawData) => {
      try {
        if (typeof rawData !== 'string') return null;

        // STOMP over WebSocket 형식 확인 (예: a["MESSAGE\n..."] 또는 a['MESSAGE\n...'] 또는 ["MESSAGE\n..."])
        let stompFrame = null;

        // 이스케이프 문자 처리 함수
        const unescapeString = (str) => {
          return str.replace(/\\(.)/g, (match, char) => {
            if (char === 'n') return '\n';
            if (char === 'r') return '\r';
            if (char === 't') return '\t';
            if (char === '\\') return '\\';
            if (char === '"') return '"';
            if (char === "'") return String.fromCharCode(39);
            if (char === 'u') {
              // 유니코드 이스케이프 처리 (\u0000 등)
              const unicodeMatch = match.match(/\\u([0-9a-fA-F]{4})/);
              if (unicodeMatch) {
                return String.fromCharCode(parseInt(unicodeMatch[1], 16));
              }
            }
            return char;
          });
        };

        // a["..."] 형식
        const doubleQuotePattern = /^a\["((?:[^"\\]|\\.)*)"\]$/;
        // a['...'] 형식
        const singleQuotePattern = /^a\['((?:[^'\\]|\\.)*)'\]$/;
        // ["..."] 형식 (배열 문자열)
        const arrayDoubleQuotePattern = /^\["((?:[^"\\]|\\.)*)"\]$/;
        // ['...'] 형식 (배열 문자열)
        const arraySingleQuotePattern = /^\['((?:[^'\\]|\\.)*)'\]$/;

        let match = rawData.match(doubleQuotePattern);
        if (match) {
          console.log('[parseStompMessage] a["..."] 패턴 매칭 성공');
          stompFrame = unescapeString(match[1]);
        } else {
          match = rawData.match(singleQuotePattern);
          if (match) {
            console.log("[parseStompMessage] a['...'] 패턴 매칭 성공");
            stompFrame = unescapeString(match[1]);
          } else {
            match = rawData.match(arrayDoubleQuotePattern);
            if (match) {
              console.log('[parseStompMessage] ["..."] 패턴 매칭 성공');
              stompFrame = unescapeString(match[1]);
            } else {
              match = rawData.match(arraySingleQuotePattern);
              if (match) {
                console.log("[parseStompMessage] ['...'] 패턴 매칭 성공");
                stompFrame = unescapeString(match[1]);
              } else {
                console.log('[parseStompMessage] 배열 패턴 매칭 실패', {
                  rawDataStart: rawData.substring(0, 50),
                  rawDataLength: rawData.length,
                });
              }
            }
          }
        }

        // a["..."] 또는 a['...'] 형식이 아니면, 직접 STOMP 프레임 형식인지 확인
        if (!stompFrame) {
          // STOMP 프레임이 직접 문자열로 오는 경우 (예: "CONNECT\n...")
          const stompCommands = [
            'CONNECT',
            'CONNECTED',
            'SEND',
            'SUBSCRIBE',
            'UNSUBSCRIBE',
            'MESSAGE',
            'RECEIPT',
            'ERROR',
            'DISCONNECT',
          ];
          const firstLine = rawData.split('\n')[0].trim();
          if (stompCommands.includes(firstLine)) {
            stompFrame = rawData;
          } else {
            return null;
          }
        }

        if (!stompFrame) return null;

        // 헤더와 본문 분리 (빈 줄로 구분)
        const headerBodySplit = stompFrame.indexOf('\n\n');

        let headersPart;
        let bodyPart = '';

        if (headerBodySplit === -1) {
          // 본문이 없는 경우 (빈 줄 없음)
          headersPart = stompFrame;
          bodyPart = '';
        } else {
          headersPart = stompFrame.substring(0, headerBodySplit);
          bodyPart = stompFrame.substring(headerBodySplit + 2);

          // 끝의 null 문자 제거 (STOMP 프레임 종료 문자)
          if (bodyPart.length > 0 && bodyPart.charCodeAt(bodyPart.length - 1) === 0) {
            bodyPart = bodyPart.substring(0, bodyPart.length - 1);
          }
        }

        // 헤더 파싱
        const headerLines = headersPart.split('\n');
        if (headerLines.length === 0) return null;

        const command = headerLines[0].trim();

        // 지원하는 모든 STOMP 명령어 파싱
        const supportedCommands = [
          'CONNECT',
          'CONNECTED',
          'SEND',
          'SUBSCRIBE',
          'UNSUBSCRIBE',
          'MESSAGE',
          'RECEIPT',
          'ERROR',
          'DISCONNECT',
        ];
        if (!supportedCommands.includes(command)) {
          return null;
        }

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

        // 명령어도 헤더에 포함
        headers['command'] = command;

        // 본문은 원본 그대로 저장 (포맷팅은 UI에서만)
        return {
          headers,
          body: bodyPart, // 원본 본문 저장
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
          stompBody = stompParsed.body; // 원본 본문 저장
          data = event.data; // 원본 데이터 저장
        } else if (typeof data === 'string') {
          // 일반 메시지는 원본 그대로 저장
          // 포맷팅은 UI에서만 할 것
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

    // WebSocket send 메서드 훅킹
    const originalSend = ws.send.bind(ws);
    ws.send = function (data) {
      try {
        let sendData = data;
        let isStompMessage = false;
        let stompHeaders = undefined;
        let stompBody = undefined;

        // 데이터 형식 변환
        if (typeof data !== 'string') {
          // eslint-disable-next-line no-undef
          if (typeof Blob !== 'undefined' && data instanceof Blob) {
            sendData = '[Blob data]';
          } else if (typeof ArrayBuffer !== 'undefined' && data instanceof ArrayBuffer) {
            sendData = '[ArrayBuffer data]';
          } else {
            sendData = String(data);
          }
        }

        // STOMP 메시지 파싱 시도
        const stompParsed = parseStompMessage(sendData);
        console.log('[WebSocket Hook] SEND 메시지 파싱 시도:', {
          sendData: typeof sendData === 'string' ? sendData.substring(0, 200) : sendData,
          sendDataLength: typeof sendData === 'string' ? sendData.length : 'N/A',
          parsed: stompParsed,
        });
        if (stompParsed) {
          isStompMessage = true;
          stompHeaders = stompParsed.headers;
          stompBody = stompParsed.body;
          console.log('[WebSocket Hook] SEND 메시지 파싱 성공:', {
            isStompMessage,
            stompHeaders,
            stompBody: stompBody ? stompBody.substring(0, 200) : stompBody,
          });
        } else {
          console.log('[WebSocket Hook] SEND 메시지 파싱 실패 - 일반 메시지로 처리');
        }

        // 보낸 메시지를 messages 배열에 추가
        const message = {
          type: 'sent',
          data: sendData,
          timestamp: Date.now(),
          isStompMessage,
          stompHeaders,
          stompBody,
        };
        console.log('[WebSocket Hook] SEND 메시지 객체 생성:', {
          type: message.type,
          isStompMessage: message.isStompMessage,
          hasStompHeaders: !!message.stompHeaders,
          hasStompBody: message.stompBody !== undefined,
        });
        // messages 배열에 직접 추가 (객체 참조가 공유되므로 collector에도 반영됨)
        messages.push(message);
      } catch {
        /* noop */
      }

      // 원본 send 메서드 호출
      return originalSend.call(this, data);
    };
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
