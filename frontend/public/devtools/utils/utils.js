/* eslint-env browser */

/**
 * Request 정보 추출 유틸리티
 */
export const extractRequestInfo = (input, init) => {
  try {
    if (typeof input === 'string') return { method: (init && init.method) || 'GET', url: input };
    if (input && input.url) return { method: input.method || 'GET', url: input.url };
  } catch {
    /* noop */
  }
  return { method: 'GET', url: String(input) };
};

/**
 * 고유 ID 생성 유틸리티
 */
export const generateRequestId = () => {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
};
