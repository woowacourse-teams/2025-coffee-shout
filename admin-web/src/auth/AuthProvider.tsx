import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { api, ApiError } from '@/api/client';
import type { AdminMe, AdminToken } from '@/api/types';
import { clearToken, readToken, saveToken } from '@/auth/tokenStore';
import { disableGoogleAutoSelect } from '@/auth/google';

type AuthState =
  /** 저장된 토큰이 아직 유효한지 확인하는 중. 이 동안 화면을 그리면 깜빡인다. */
  | { status: 'loading' }
  | { status: 'authenticated'; email: string }
  | { status: 'anonymous' };

type AuthContextValue = AuthState & {
  /** 구글 ID 토큰을 관리자 토큰으로 교환한다. */
  loginWithGoogle: (idToken: string) => Promise<void>;
  /** local 프로필 전용 우회 경로. 구글 클라이언트 없이 개발할 때 쓴다. */
  loginWithDevEmail: (email: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading' });

  /**
   * 새로고침할 때마다 저장된 토큰이 아직 살아 있는지 서버에 묻는다.
   * 만료된 토큰으로 화면을 그리면 첫 조회가 401 로 떨어지면서 빈 화면이 먼저 보인다.
   */
  useEffect(() => {
    if (!readToken()) {
      setState({ status: 'anonymous' });
      return;
    }

    let cancelled = false;
    api
      .get<AdminMe>('/auth/me')
      .then((me) => {
        if (!cancelled) setState({ status: 'authenticated', email: me.email });
      })
      .catch(() => {
        // client 가 401 에서 이미 토큰을 지운다. 여기서는 상태만 맞춘다.
        if (!cancelled) setState({ status: 'anonymous' });
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const exchange = useCallback(async (issue: () => Promise<AdminToken>) => {
    const { accessToken } = await issue();
    saveToken(accessToken);
    const me = await api.get<AdminMe>('/auth/me');
    setState({ status: 'authenticated', email: me.email });
  }, []);

  const loginWithGoogle = useCallback(
    (idToken: string) => exchange(() => api.post<AdminToken>('/auth/login', { idToken })),
    [exchange],
  );

  const loginWithDevEmail = useCallback(
    (email: string) => exchange(() => api.post<AdminToken>('/auth/dev-login', { email })),
    [exchange],
  );

  const logout = useCallback(() => {
    clearToken();
    // 다음 로그인에서 계정 선택 화면이 다시 뜨게 한다. 이것을 안 하면 방금 로그아웃한
    // 계정으로 곧바로 다시 들어가진다.
    disableGoogleAutoSelect();
    setState({ status: 'anonymous' });
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ ...state, loginWithGoogle, loginWithDevEmail, logout }),
    [state, loginWithGoogle, loginWithDevEmail, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth 는 AuthProvider 안에서만 쓸 수 있습니다.');
  }
  return context;
}

/** 로그인 실패 메시지를 사람이 읽을 문장으로. */
export function toLoginErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.isForbidden) {
      return '관리자 허용목록에 없는 계정입니다.';
    }
    return error.message;
  }
  return '로그인에 실패했습니다. 잠시 후 다시 시도해주세요.';
}
