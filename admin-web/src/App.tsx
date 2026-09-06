import { QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { ApiError } from '@/api/client';
import { AuthProvider, useAuth } from '@/auth/AuthProvider';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { Skeleton } from '@/components/ui/EmptyState';
import { DesignGalleryPage } from '@/pages/DesignGalleryPage';
import { HomePage } from '@/pages/HomePage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error) => {
      // 401 은 화면마다 처리하지 않는다. 토큰은 client 가 이미 지웠고,
      // 다음 렌더에서 RequireAuth 가 로그인으로 보낸다.
      if (error instanceof ApiError && error.isUnauthorized) {
        queryClient.clear();
      }
    },
  }),
  defaultOptions: {
    queries: {
      // 운영 화면은 오래된 숫자를 보여주면 안 된다. 조치하고 돌아왔을 때 이전 값이
      // 남아 있으면 조치가 안 먹은 것으로 오해한다.
      staleTime: 10_000,
      refetchOnWindowFocus: true,
      // 인증/권한 오류는 재시도해도 결과가 같다. 한 번 더 물어봐야 사용자만 기다린다.
      retry: (failureCount, error) =>
        error instanceof ApiError && (error.isUnauthorized || error.isForbidden)
          ? false
          : failureCount < 1,
    },
  },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<RequireAuth />}>
              <Route element={<AdminLayout />}>
                <Route index element={<HomePage />} />
                <Route path="/design" element={<DesignGalleryPage />} />
                <Route path="*" element={<NotFoundPage />} />
              </Route>
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

/**
 * 로그인 확인이 끝나기 전에는 아무것도 그리지 않는다.
 * 확인 중에 화면을 그리면 로그인 상태인데도 로그인 화면이 한 번 스쳐 지나간다.
 */
function RequireAuth() {
  const auth = useAuth();

  if (auth.status === 'loading') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-canvas">
        <Skeleton className="h-8 w-32" />
      </div>
    );
  }

  if (auth.status === 'anonymous') {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
