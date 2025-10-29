/**
 * 네트워크 요청 수집 및 저장
 */

export type RequestType = 'fetch' | 'websocket';
export type RequestContext = 'MAIN' | string; // 'MAIN' 또는 iframe name

export interface NetworkRequest {
  id: string;
  type: RequestType;
  context: RequestContext;
  method?: string;
  url: string;
  status?: number;
  timestamp: number;
  // 요청 데이터
  requestHeaders?: Record<string, string>;
  requestBody?: string;
  queryParams?: string;
  // 응답 데이터
  responseHeaders?: Record<string, string>;
  responseBody?: string;
  // WebSocket 데이터
  data?: unknown;
}

type RequestListener = (request: NetworkRequest) => void;

class NetworkCollector {
  private requests: NetworkRequest[] = [];
  private listeners: Set<RequestListener> = new Set();
  private maxRequests = 1000; // 최대 저장 요청 수

  addRequest(request: NetworkRequest): void {
    this.requests.unshift(request); // 최신 요청을 앞에 추가

    // 최대 개수 제한
    if (this.requests.length > this.maxRequests) {
      this.requests = this.requests.slice(0, this.maxRequests);
    }

    // 리스너들에게 알림
    this.listeners.forEach((listener) => listener(request));
  }

  getRequests(): NetworkRequest[] {
    return [...this.requests];
  }

  clear(): void {
    this.requests = [];
    // clear 이벤트를 리스너에게 전달할 수도 있음
  }

  subscribe(listener: RequestListener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }
}

export const networkCollector = new NetworkCollector();

/**
 * window 객체에 collector를 전달하는 함수
 * injected script에서 사용
 */
export const getCollectorScript = (): string => {
  return `
    (() => {
      const collector = window.__networkCollector__;
      if (!collector) return;
      
      // 이미 인터셉트했으면 스킵 (중복 방지)
      if (window.__networkInterceptorInjected__) return;
      window.__networkInterceptorInjected__ = true;
      
      const context = window.self === window.top ? 'MAIN' : (window.name || 'IFRAME');
      
      // Fetch 인터셉트
      const originalFetch = window.fetch;
      window.fetch = (...args) => {
        let url = '';
        let options = {};
        
        // 첫 번째 인자가 Request 객체인지 string인지 확인
        if (typeof args[0] === 'string') {
          url = args[0];
          options = args[1] || {};
        } else if (args[0] instanceof Request) {
          url = args[0].url;
          options = {
            method: args[0].method,
            headers: args[0].headers,
            body: args[0].body,
            ...(args[1] || {}), // 옵션 병합
          };
        } else {
          url = args[0]?.url || '';
          options = args[1] || {};
        }
        
        const method = options.method || 'GET';
        
        // URL 파싱 (query params 분리)
        let urlObj;
        try {
          urlObj = new URL(url, window.location.href);
        } catch {
          // URL 파싱 실패 시 원본 URL 사용
          urlObj = new URL(window.location.href);
          urlObj.pathname = url;
        }
        const queryParams = urlObj.search;
        const baseUrl = urlObj.origin + urlObj.pathname;
        
        // 요청 헤더 수집
        const requestHeaders = {};
        if (options.headers) {
          if (options.headers instanceof Headers) {
            options.headers.forEach((value, key) => {
              requestHeaders[key] = value;
            });
          } else if (Array.isArray(options.headers)) {
            options.headers.forEach(([key, value]) => {
              requestHeaders[key] = value;
            });
          } else {
            Object.assign(requestHeaders, options.headers);
          }
        }
        
        // 요청 body 수집
        let requestBody = '';
        if (options.body) {
          if (typeof options.body === 'string') {
            requestBody = options.body;
          } else if (options.body instanceof FormData || options.body instanceof URLSearchParams) {
            // FormData나 URLSearchParams는 문자열화 불가
            requestBody = '[FormData or URLSearchParams]';
          } else {
            try {
              requestBody = JSON.stringify(options.body);
            } catch {
              requestBody = '[Unable to stringify body]';
            }
          }
        }
        
        const requestId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
        
        // 응답 클론을 위해 변수 저장
        const requestData = {
          id: requestId,
          type: 'fetch',
          context: context,
          method: method,
          url: baseUrl,
          queryParams: queryParams || undefined,
          requestHeaders: Object.keys(requestHeaders).length > 0 ? requestHeaders : undefined,
          requestBody: requestBody || undefined,
          timestamp: Date.now(),
        };
        
        return originalFetch(...args).then(async (response) => {
          // 응답 클론 (body를 읽기 위해)
          const responseClone = response.clone();
          
          // 응답 헤더 수집
          const responseHeaders = {};
          response.headers.forEach((value, key) => {
            responseHeaders[key] = value;
          });
          
          // 응답 body 수집
          let responseBody = '';
          try {
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
              responseBody = await responseClone.json().then((data) => JSON.stringify(data, null, 2));
            } else if (contentType.includes('text/')) {
              responseBody = await responseClone.text();
            } else {
              // 바이너리 데이터는 표시 안 함
              responseBody = '[Binary data]';
            }
          } catch {
            responseBody = '[Unable to read response]';
          }
          
          // 요청-응답 통합하여 저장
          collector.addRequest({
            ...requestData,
            status: response.status,
            responseHeaders: Object.keys(responseHeaders).length > 0 ? responseHeaders : undefined,
            responseBody: responseBody || undefined,
          });
          
          return response;
        });
      };
      
      // WebSocket 인터셉트
      const OriginalWebSocket = window.WebSocket;
      window.WebSocket = function(url, protocols) {
        const ws = protocols ? new OriginalWebSocket(url, protocols) : new OriginalWebSocket(url);
        const socketId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
        
        // 연결 열림
        ws.addEventListener('open', () => {
          collector.addRequest({
            id: socketId + '_open',
            type: 'websocket',
            context: context,
            url: url,
            timestamp: Date.now(),
            data: '[WebSocket opened]',
          });
        });
        
        // 메시지 수신
        ws.addEventListener('message', (event) => {
          let messageData = event.data;
          if (typeof messageData === 'string') {
            try {
              const parsed = JSON.parse(messageData);
              messageData = JSON.stringify(parsed, null, 2);
            } catch {
              // JSON이 아니면 그대로 사용
            }
          } else {
            messageData = '[Binary data]';
          }
          
          collector.addRequest({
            id: socketId + '_msg_' + Date.now(),
            type: 'websocket',
            context: context,
            url: url,
            data: messageData,
            timestamp: Date.now(),
          });
        });
        
        // 메시지 전송
        const originalSend = ws.send;
        ws.send = function(data) {
          let sendData = data;
          if (typeof sendData === 'string') {
            try {
              const parsed = JSON.parse(sendData);
              sendData = JSON.stringify(parsed, null, 2);
            } catch {
              // JSON이 아니면 그대로 사용
            }
          } else {
            sendData = '[Binary data]';
          }
          
          collector.addRequest({
            id: socketId + '_send_' + Date.now(),
            type: 'websocket',
            context: context,
            url: url,
            data: sendData,
            timestamp: Date.now(),
          });
          
          return originalSend.call(ws, data);
        };
        
        return ws;
      };
    })();
  `;
};
