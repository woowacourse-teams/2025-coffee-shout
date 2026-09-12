const KEY = 'zzol-admin-token';

/**
 * 관리자 토큰 보관소.
 *
 * <p>`localStorage` 를 쓴다. 메모리에만 두면 새로고침마다 구글 팝업이 다시 떠야 하는데,
 * 백오피스는 표를 보다 새로고침하는 일이 잦아 그 비용이 크다.
 *
 * <p>XSS 가 나면 토큰이 털린다는 반론이 있지만, 이 앱은 사용자 입력을 innerHTML 로 그리는
 * 곳이 없고 서드파티 스크립트도 구글 하나뿐이라 실질 노출면이 좁다. 그리고 토큰 수명이
 * 1시간이고 리프레시가 없어 털려도 창이 짧다.
 *
 * <p>접근이 막힌 브라우저(시크릿 모드 설정, 저장소 차단)에서도 앱이 죽지 않아야 하므로
 * 모든 접근을 try 로 감싼다. 실패하면 "로그인 안 된 상태"로 흘러가면 된다.
 */
export function readToken(): string | null {
  try {
    return localStorage.getItem(KEY);
  } catch {
    return null;
  }
}

export function saveToken(token: string): void {
  try {
    localStorage.setItem(KEY, token);
  } catch {
    // 저장이 막힌 브라우저에서는 이 탭 안에서만 로그인 상태가 유지된다.
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    // 지우지 못해도 서버가 거절하므로 실질 위험은 없다.
  }
}
