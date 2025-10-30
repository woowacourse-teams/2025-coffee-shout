/* eslint-env browser */

(function () {
  const w = typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined;
  if (!w) return; // non-browser guard
  if (w.__DEV_SNIPPET__) return;
  w.__DEV_SNIPPET__ = true;
  w.console && w.console.log('[DEV SNIPPET] active');

  const OriginalWebSocket = w.WebSocket;
  if (!OriginalWebSocket) return;
  w.WebSocket = new Proxy(OriginalWebSocket, {
    construct(target, args) {
      const [url, protocols] = args;
      w.console && w.console.log('[DEV-SNIP] window.WebSocket 호출', url, protocols);
      return new target(url, protocols);
    },
  });
})();
