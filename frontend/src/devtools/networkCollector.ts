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
  data?: unknown;
  direction: 'sent' | 'received';
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
        const url = args[0];
        const method = args[1]?.method || 'GET';
        
        // 요청 전송
        const requestId = Date.now().toString() + Math.random().toString(36).substr(2, 9);
        collector.addRequest({
          id: requestId,
          type: 'fetch',
          context: context,
          method: method,
          url: typeof url === 'string' ? url : url.url || '',
          timestamp: Date.now(),
          direction: 'sent'
        });
        
        return originalFetch(...args).then(response => {
          // 응답 수신
          collector.addRequest({
            id: requestId + '_response',
            type: 'fetch',
            context: context,
            method: method,
            url: typeof url === 'string' ? url : url.url || '',
            status: response.status,
            timestamp: Date.now(),
            direction: 'received'
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
            direction: 'sent'
          });
        });
        
        // 메시지 수신
        ws.addEventListener('message', (event) => {
          collector.addRequest({
            id: socketId + '_msg_' + Date.now(),
            type: 'websocket',
            context: context,
            url: url,
            data: event.data,
            timestamp: Date.now(),
            direction: 'received'
          });
        });
        
        // 메시지 전송
        const originalSend = ws.send;
        ws.send = function(data) {
          collector.addRequest({
            id: socketId + '_send_' + Date.now(),
            type: 'websocket',
            context: context,
            url: url,
            data: data,
            timestamp: Date.now(),
            direction: 'sent'
          });
          
          return originalSend.call(ws, data);
        };
        
        return ws;
      };
    })();
  `;
};
