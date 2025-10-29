// snippet 자동 주입 시작 (iframe + 현재 페이지)
export const injectSnippet = (): void => {
  // 현재 페이지는 이미 로드되었으므로 바로 주입
  injectScript(window);

  // iframe 감지 및 주입
  const observer = new MutationObserver(() => {
    document.querySelectorAll('iframe').forEach((iframe) => {
      injectScript(iframe);
    });
  });

  const rootElement = document.getElementById('root');
  if (rootElement) {
    observer.observe(rootElement, {
      childList: true,
    });
  }
};
// 스크립트 주입 (기본 함수 - window 또는 iframe 모두 지원)
const injectScript = (target: Window | HTMLIFrameElement): void => {
  const document = getDocument(target);
  if (!document) return;

  const script = document.createElement('script');
  script.textContent = `
    // super minimal network logger (fetch + WebSocket)
    (() => {
      const f = window.fetch;
      window.fetch = (...a) => (console.log('fetch →', a[0]), f(...a).then(r => (console.log('fetch ←', r.status, a[0]), r)));
      const W = window.WebSocket;
      window.WebSocket = function(u, p) {
        const ws = p ? new W(u, p) : new W(u);
        ws.addEventListener('open', () => console.log('ws open', u));
        ws.addEventListener('message', e => console.log('ws ←', u, e.data));
        const s = ws.send;
        ws.send = d => (console.log('ws →', u, d), s.call(ws, d));
        return ws;
      };
    })();
  `;
  document.head.appendChild(script);
};

const getDocument = (target: Window | HTMLIFrameElement) => {
  if (target instanceof Window) {
    return target.document;
  } else {
    return target.contentDocument;
  }
};
