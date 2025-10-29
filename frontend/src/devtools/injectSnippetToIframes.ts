// 스크립트 주입 (기본 함수 - window 또는 iframe 모두 지원)
const injectScript = (target: Window | HTMLIFrameElement): void => {
  const document = target instanceof Window ? target.document : target.contentDocument;
  if (!document) return;

  const script = document.createElement('script');
  script.textContent = `
    setInterval(() => {
      console.log('Injected script running', new Date());
    }, 1000);
  `;
  document.head.appendChild(script);
};

// 로드 완료 후 주입 (window 또는 iframe)
const injectWhenReady = (target: Window | HTMLIFrameElement): void => {
  const document = getDocument(target);

  // 이미 로드된 경우 바로 주입
  if (document && document.readyState === 'complete') {
    injectScript(target);
    return;
  }

  // 로드 완료 대기
  target.addEventListener('load', () => {
    injectScript(target);
  });
};

// snippet 자동 주입 시작 (iframe + 현재 페이지)
export const injectSnippet = (): void => {
  injectWhenReady(window);

  // iframe 감지 및 주입
  const observer = new MutationObserver(() => {
    document.querySelectorAll('iframe').forEach((iframe) => {
      injectWhenReady(iframe);
    });
  });

  const rootElement = document.getElementById('root');
  if (rootElement) {
    observer.observe(rootElement, {
      childList: true,
    });
  }
};

const getDocument = (target: Window | HTMLIFrameElement) => {
  if (target instanceof Window) {
    return target.document;
  } else {
    return target.contentDocument;
  }
};
