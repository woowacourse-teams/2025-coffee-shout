import { networkCollector, getCollectorScript } from './networkCollector';

// 중복 주입 방지
let isInjected = false;
let observer: MutationObserver | null = null;

// window 또는 iframe 로드 완료 후 주입
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
  // 이미 주입했으면 스킵
  if (isInjected) return;

  // window에 collector 노출
  (window as Window & { __networkCollector__?: typeof networkCollector }).__networkCollector__ =
    networkCollector;

  // window 로드 완료 후 주입
  injectWhenReady(window);

  // iframe 감지 및 주입
  observer = new MutationObserver(() => {
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

  isInjected = true;
};

// 이미 주입한 target 추적
const injectedTargets = new WeakSet<Window | HTMLIFrameElement>();

// 스크립트 주입 (기본 함수 - window 또는 iframe 모두 지원)
const injectScript = (target: Window | HTMLIFrameElement): void => {
  // 이미 주입했으면 스킵
  if (injectedTargets.has(target)) return;

  const document = getDocument(target);
  if (!document) return;

  // collector 전달 스크립트
  const collectorScript = document.createElement('script');
  collectorScript.textContent = `
    if (!window.__networkCollector__) {
      window.__networkCollector__ = window.parent?.__networkCollector__ || null;
    }
    ${getCollectorScript()}
  `;

  document.head.appendChild(collectorScript);
  injectedTargets.add(target);
};

const getDocument = (target: Window | HTMLIFrameElement) => {
  if (target instanceof Window) {
    return target.document;
  } else {
    return target.contentDocument;
  }
};
