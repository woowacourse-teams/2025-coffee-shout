/* eslint-env browser */

import { extractRequestInfo, generateRequestId, maskHeaders } from '../utils/utils.js';

/**
 * Fetch API를 후킹하여 네트워크 요청을 수집합니다.
 * 원본 Promise를 그대로 반환하여 호출자에 전혀 영향을 주지 않습니다.
 */
export const setupFetchHook = (win, collector, context = {}) => {
  win =
    win || (typeof globalThis !== 'undefined' && globalThis.window ? globalThis.window : undefined);
  if (!win || typeof win.fetch !== 'function') {
    return null;
  }

  if (win.__DEV_FETCH_WRAPPED__) {
    return win.__DEV_FETCH_WRAPPED__;
  }

  const originalFetch = win.fetch;

  const originalDescriptor = Object.getOwnPropertyDescriptor(win, 'fetch') || {
    configurable: true,
    enumerable: false,
    writable: true,
    value: originalFetch,
  };

  const wrapper = function (input, init = {}) {
    const startedAt = Date.now();
    let method = 'GET';
    let url = '';

    try {
      ({ method, url } = extractRequestInfo(input, init));
    } catch {
      /* noop */
    }

    // 원본 Promise를 얻는다 — 이 Promise를 그대로 반환(호출자에 영향 없음)
    const originalPromise = Reflect.apply(originalFetch, this, [input, init]);

    // 원본 Promise에 즉시 then/catch를 붙여 "비동기 기록 작업" 수행
    originalPromise
      .then((res) => {
        // 비동기로 처리하되, 여기서 절대 원본 Promise를 block 하지 않음
        (async () => {
          try {
            // opaque/cors 체크: content-type 얻기 시도
            const ct =
              res && res.headers && typeof res.headers.get === 'function'
                ? res.headers.get('content-type') || ''
                : '';

            let responseBody = null;

            if (res && typeof res.clone === 'function' && ct) {
              try {
                const cloned = res.clone();

                if (ct.includes('application/json') || ct.startsWith('text/')) {
                  const txt = await cloned.text();
                  responseBody = txt.slice(0, 2048);
                }
              } catch {
                responseBody = null;
              }
            }

            // 요청 헤더 마스킹
            const requestHeaders = (() => {
              try {
                if (!init || !init.headers) {
                  return {};
                }

                // eslint-disable-next-line no-undef
                if (typeof Headers === 'function' && init.headers instanceof Headers) {
                  const obj = {};
                  for (const [k, v] of init.headers.entries()) {
                    obj[k] = v;
                  }
                  return maskHeaders(obj);
                }

                return maskHeaders({ ...init.headers });
              } catch {
                return {};
              }
            })();

            collector.add({
              id: generateRequestId(),
              type: 'fetch',
              context,
              method,
              url,
              status: res.status,
              timestamp: Date.now(),
              responseBody,
              durationMs: Date.now() - startedAt,
              requestHeaders,
              responseHeaders:
                res.headers && typeof res.headers.get === 'function'
                  ? { 'content-type': res.headers.get('content-type') || null }
                  : undefined,
            });
          } catch (err) {
            // 로그 실패는 절대 원본 흐름에 영향 주지 않음
            try {
              collector.add({
                id: generateRequestId(),
                type: 'fetch',
                context,
                method,
                url,
                status: 'LOG_ERROR',
                error: String(err).slice(0, 512),
              });
            } catch {
              /* noop */
            }
          }
        })();

        return res; // then 핸들에서는 원본 res를 그대로 반환(체인 무결성 유지)
      })
      .catch((err) => {
        // 네트워크/거부 에러도 기록하되 원본 에러는 그대로 전파(우린 반환하지 않음)
        try {
          collector.add({
            id: generateRequestId(),
            type: 'fetch',
            context,
            method,
            url,
            status: 'NETWORK_ERROR',
            timestamp: Date.now(),
            durationMs: Date.now() - startedAt,
            errorMessage: String(err).slice(0, 512),
            requestHeaders: maskHeaders((init && init.headers) || {}),
          });
        } catch {
          /* noop */
        }
        // 원본 Promise의 reject는 호출자에게 전달되도록 여기서 rethrow 하지 않음(우리는 단순 관찰자)
      });

    // 핵심: originalPromise를 그대로 반환 -> 호출자에는 완전히 투명함
    return originalPromise;
  };

  // wrapper의 name/length 보존 (선택)
  try {
    Object.defineProperty(wrapper, 'name', {
      value: originalFetch.name,
      configurable: true,
    });
    Object.defineProperty(wrapper, 'length', {
      value: originalFetch.length,
      configurable: true,
    });
  } catch {
    /* noop */
  }

  // 프로퍼티 교체
  Object.defineProperty(win, 'fetch', {
    configurable: true,
    enumerable: originalDescriptor.enumerable,
    writable: originalDescriptor.writable,
    value: wrapper,
  });

  const handle = {
    restore() {
      try {
        Object.defineProperty(win, 'fetch', originalDescriptor);
        delete win.__DEV_FETCH_WRAPPED__;
      } catch {
        /* noop */
      }
    },
  };

  win.__DEV_FETCH_WRAPPED__ = handle;

  return handle;
};
