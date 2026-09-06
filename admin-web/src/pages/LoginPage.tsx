import { useEffect, useRef, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { toLoginErrorMessage, useAuth } from '@/auth/AuthProvider';
import { isGoogleConfigured, renderGoogleButton } from '@/auth/google';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Field';
import { EnvBadge } from '@/components/ui/EnvBadge';
import { ENV_NAME } from '@/lib/env';

/**
 * 로그인. 화면에 아무것도 더 두지 않는다.
 *
 * <p>백오피스 로그인 화면은 하루에 한 번 보고 지나가는 곳이라 꾸밀 이유가 없다.
 * 대신 <b>어느 환경인지</b>는 크게 보여준다. prod 백오피스에 들어가고 있다는 사실을
 * 로그인 전에 알아야 한다.
 */
export function LoginPage() {
  const auth = useAuth();
  const googleButtonRef = useRef<HTMLDivElement>(null);
  const [error, setError] = useState<string | null>(null);
  const [devEmail, setDevEmail] = useState('');
  const [pending, setPending] = useState(false);

  useEffect(() => {
    if (!isGoogleConfigured() || !googleButtonRef.current) {
      return;
    }
    renderGoogleButton(googleButtonRef.current, (idToken) => {
      setPending(true);
      setError(null);
      auth
        .loginWithGoogle(idToken)
        .catch((cause) => setError(toLoginErrorMessage(cause)))
        .finally(() => setPending(false));
    }).catch((cause) => setError(toLoginErrorMessage(cause)));
  }, [auth]);

  if (auth.status === 'authenticated') {
    return <Navigate to="/" replace />;
  }

  const submitDevLogin = (event: React.FormEvent) => {
    event.preventDefault();
    setPending(true);
    setError(null);
    auth
      .loginWithDevEmail(devEmail)
      .catch((cause) => setError(toLoginErrorMessage(cause)))
      .finally(() => setPending(false));
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-canvas px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex items-center gap-2.5">
          <img src="/brand/logo.svg" alt="ZZOL" className="h-5" />
          <span className="text-sm font-medium text-ink-muted">백오피스</span>
          <EnvBadge className="ml-auto" />
        </div>

        <div className="rounded-lg border border-border-default bg-surface p-6">
          <h1 className="text-lg font-semibold tracking-tight text-ink">로그인</h1>
          <p className="mt-1 text-xs leading-relaxed text-ink-muted">
            허용목록에 등록된 구글 계정만 들어올 수 있습니다.
          </p>

          {isGoogleConfigured() ? (
            <div className="mt-5 flex justify-center" ref={googleButtonRef} />
          ) : (
            <p className="mt-5 rounded-md border border-border-default bg-subtle px-3 py-2 text-xs text-ink-secondary">
              구글 클라이언트 ID가 설정되지 않았습니다.
              <br />
              <code className="font-mono">VITE_GOOGLE_CLIENT_ID</code> 를 채우거나 아래 로컬
              로그인을 쓰세요.
            </p>
          )}

          {/* local 에서만 노출한다. 배포 번들에도 이 코드가 들어가지만 서버가
            * dev-login 엔드포인트를 아예 등록하지 않아 눌러도 401 이다. */}
          {ENV_NAME === 'LOCAL' && (
            <form onSubmit={submitDevLogin} className="mt-5 border-t border-border-default pt-5">
              <p className="text-xs font-medium text-ink-secondary">로컬 로그인</p>
              <p className="mt-0.5 text-2xs text-ink-muted">
                구글 검증 없이 허용목록만 확인합니다. local 프로필에서만 동작합니다.
              </p>
              <div className="mt-2 flex gap-2">
                <Input
                  type="email"
                  required
                  value={devEmail}
                  onChange={(event) => setDevEmail(event.target.value)}
                  placeholder="admin@zzol.site"
                />
                <Button type="submit" variant="primary" disabled={pending}>
                  들어가기
                </Button>
              </div>
            </form>
          )}

          {error && (
            <p className="mt-4 rounded-md bg-attention-bg px-3 py-2 text-xs text-attention">
              {error}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
