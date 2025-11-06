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

/**
 * 네트워크 요청을 공통 포맷으로 컬렉터에 추가합니다.
 * 모든 네트워크 요청 타입(fetch, websocket, stomp 등)에서 공통 필드를 보장합니다.
 */
export const addRequest = (collector, data) => {
  const now = Date.now();

  const base = {
    id: generateRequestId(),
    timestamp: now,
    durationMs: 0,
    type: 'unknown',
    status: null,
    context: {},
    url: '',
  };

  // durationMs 계산: data.durationMs가 있으면 사용, 없으면 startedAt 기반 계산
  let durationMs = data.durationMs;
  if (durationMs === undefined && data.startedAt) {
    durationMs = now - data.startedAt;
  }

  const request = {
    ...base,
    ...data,
    durationMs: durationMs ?? 0,
  };

  collector.add(request);
  return request;
};
