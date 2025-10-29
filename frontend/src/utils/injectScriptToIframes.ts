// iframe에 스크립트 주입
const injectScript = (iframe: HTMLIFrameElement): void => {
  if (!iframe.contentDocument) return;

  const script = iframe.contentDocument.createElement('script');
  script.textContent = `
    setInterval(() => {
      console.log('Injected script running', new Date());
    }, 1000);
  `;
  iframe.contentDocument.head.appendChild(script);
};

// iframe 로드 완료 후 주입
const injectWhenReady = (iframe: HTMLIFrameElement): void => {
  // 이미 로드된 경우 바로 주입
  if (iframe.contentDocument && iframe.contentDocument.readyState === 'complete') {
    injectScript(iframe);
    return;
  }

  // 로드 완료 대기
  iframe.addEventListener('load', () => {
    injectScript(iframe);
  });
};

// iframe에 snippet 자동 주입 시작
export const injectSnippetToIframes = (): void => {
  const observer = new MutationObserver(() => {
    document.querySelectorAll('iframe').forEach((iframe) => {
      injectWhenReady(iframe);
    });
  });

  const rootElement = document.getElementById('root');
  if (rootElement) {
    observer.observe(rootElement, {
      childList: true,
      subtree: true,
    });
  }
};
