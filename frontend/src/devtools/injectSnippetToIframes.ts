import { networkCollector, getCollectorScript } from './networkCollector';

// 이미 주입했는지 확인하는 플래그
let isInjected = false;
let observer: MutationObserver | null = null;

// snippet 자동 주입 시작 (iframe + 현재 페이지)
export const injectSnippet = (): void => {
  // 이미 주입했으면 스킵 (Strict Mode 중복 방지)
  if (isInjected) return;

  // window에 collector 노출
  (window as Window & { __networkCollector__?: typeof networkCollector }).__networkCollector__ =
    networkCollector;

  // 현재 페이지는 이미 로드되었으므로 바로 주입
  injectScript(window);

  // iframe 감지 및 주입
  observer = new MutationObserver(() => {
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
    window.__networkCollector__ = window.parent.__networkCollector__ || window.__networkCollector__;
    ${getCollectorScript()}
  `;

  // collector가 없는 경우 빈 객체로 초기화 (iframe에서)
  const targetWindow = target as Window & { __networkCollector__?: typeof networkCollector };
  if (!targetWindow.__networkCollector__) {
    const initScript = document.createElement('script');
    initScript.textContent = `
      if (!window.__networkCollector__) {
        window.__networkCollector__ = window.parent?.__networkCollector__ || null;
      }
    `;
    document.head.appendChild(initScript);
  }

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
