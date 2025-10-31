/* eslint-env browser */

import { extractRequestInfo, generateRequestId } from '../utils/utils.js';

/**
 * Fetch API를 후킹하여 네트워크 요청을 수집합니다.
 */
export const setupFetchHook = (window, collector, context) => {
  const OriginalFetch = window.fetch;
  if (typeof OriginalFetch !== 'function' || window.__DEV_FETCH_WRAPPED__) {
    return;
  }

  window.__DEV_FETCH_WRAPPED__ = true;
  window.fetch = async (input, init) => {
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

    collector.add({
      id: generateRequestId(),
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
};
