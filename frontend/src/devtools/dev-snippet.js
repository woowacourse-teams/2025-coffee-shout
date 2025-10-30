/* eslint-env browser */

(function () {
  const w = typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined;
  if (!w) return; // non-browser guard
  if (w.__DEV_SNIPPET__) return;
  w.__DEV_SNIPPET__ = true;
  w.console && w.console.log('[DEV SNIPPET] active');

  // ----------------------------------------------------------------------------
  // NetworkCollector (observer)
  // ----------------------------------------------------------------------------
  class NetworkCollector {
    constructor(max = 1000) {
      this.max = max;
      this.requests = [];
      this.listeners = new Set();
    }
    add(request) {
      this.requests.unshift(request);
      if (this.requests.length > this.max) this.requests = this.requests.slice(0, this.max);
      this.listeners.forEach((fn) => {
        try {
          fn(request);
        } catch {
          /* noop */
        }
      });
    }
    getRequests() {
      return this.requests.slice();
    }
    clear() {
      this.requests = [];
    }
    subscribe(listener) {
      this.listeners.add(listener);
      return () => this.listeners.delete(listener);
    }
  }

  if (!w.__networkCollector__) {
    w.__networkCollector__ = new NetworkCollector(1000);
  }

  // simple console subscriber
  if (!w.__networkCollectorConsoleSub__) {
    w.__networkCollectorConsoleSub__ = w.__networkCollector__.subscribe((req) => {
      try {
        w.console && w.console.log('[NET]', req.type, req);
      } catch {
        /* noop */
      }
    });
  }

  const context = w.self === w.top ? 'MAIN' : w.name || 'IFRAME';

  // fetch hook
  const OriginalFetch = w.fetch;
  if (typeof OriginalFetch === 'function' && !w.__DEV_FETCH_WRAPPED__) {
    w.__DEV_FETCH_WRAPPED__ = true;
    w.fetch = async function (input, init) {
      const startedAt = Date.now();
      const { method, url } = extractRequestInfo(input, init);
      const res = await OriginalFetch(input, init);
      const durationMs = Date.now() - startedAt;
      let responseBody;
      try {
        const cloned = res.clone();
        const ct = res.headers && res.headers.get ? res.headers.get('content-type') : '';
        if (ct && (ct.includes('application/json') || ct.startsWith('text/'))) {
          responseBody = (await cloned.text()).slice(0, 2048);
        }
      } catch {
        /* noop */
      }
      w.__networkCollector__.add({
        id: rid(),
        type: 'fetch',
        context,
        method,
        url,
        status: res.status,
        timestamp: Date.now(),
        responseBody,
        durationMs,
      });
      return res;
    };
  }

  // WebSocket hook
  const OriginalWebSocket = w.WebSocket;
  if (OriginalWebSocket && !w.__DEV_WS_WRAPPED__) {
    w.__DEV_WS_WRAPPED__ = true;
    w.WebSocket = new Proxy(OriginalWebSocket, {
      construct(target, args) {
        const [url, protocols] = args;
        const ws = new target(url, protocols);
        try {
          ws.addEventListener('open', () => {
            w.__networkCollector__.add({
              id: rid(),
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
            w.__networkCollector__.add({
              id: rid(),
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
  }

  function extractRequestInfo(input, init) {
    try {
      if (typeof input === 'string') return { method: (init && init.method) || 'GET', url: input };
      if (input && input.url) return { method: input.method || 'GET', url: input.url };
    } catch {
      /* noop */
    }
    return { method: 'GET', url: String(input) };
  }

  function rid() {
    return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }
})();
