import { Suspense } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  AlertTriangle,
  Gamepad2,
  History,
  LayoutDashboard,
  LogOut,
  MessageSquareWarning,
  ScrollText,
  ServerCog,
  Search,
  ShieldBan,
  SpellCheck,
  Users,
  UserCog,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { useAuth } from '@/auth/AuthProvider';
import { EnvBadge } from '@/components/ui/EnvBadge';
import { Skeleton } from '@/components/ui/EmptyState';

type NavItem = { to: string; label: string; icon: LucideIcon };
type NavGroup = { heading: string; items: NavItem[] };

/**
 * 레일 그룹은 <b>업무 흐름</b>으로 묶는다. 알파벳순이나 만든 순이 아니다.
 * 운영자는 "지금 처리할 것 → 무슨 일이 있었나 → 도구" 순으로 화면을 옮긴다.
 */
const NAV: NavGroup[] = [
  {
    heading: '운영',
    items: [
      { to: '/', label: '홈', icon: LayoutDashboard },
      { to: '/reports', label: '신고', icon: MessageSquareWarning },
      { to: '/profanity', label: '닉네임 검열', icon: SpellCheck },
      { to: '/ip-blocks', label: 'IP 차단', icon: ShieldBan },
    ],
  },
  {
    heading: '조회',
    items: [
      { to: '/rooms', label: '방 조회', icon: Search },
      { to: '/users', label: '유저', icon: Users },
      { to: '/games', label: '서비스 분석', icon: Gamepad2 },
    ],
  },
  {
    heading: '관리',
    items: [
      { to: '/patch-notes', label: '패치노트', icon: ScrollText },
      { to: '/zzolbot', label: 'ZzolBot', icon: AlertTriangle },
      { to: '/admins', label: '관리자', icon: UserCog },
      { to: '/ops', label: '시스템', icon: ServerCog },
      { to: '/audit-logs', label: '조치 이력', icon: History },
    ],
  },
];

export function AdminLayout() {
  const { pathname } = useLocation();
  const current = NAV.flatMap((group) => group.items).find((item) =>
    item.to === '/' ? pathname === '/' : pathname.startsWith(item.to),
  );

  return (
    <div className="min-h-screen bg-canvas">
      <Rail />

      <div className="pl-rail-offset">
        {/* 캔버스 위에 그대로 얹는다. 흰 바탕에 경계선을 두면 상단 바가 또 하나의 카드처럼
          * 보여서, 정작 봐야 할 카드들과 같은 무게를 갖는다. 스크롤할 때 글자가 겹치지
          * 않도록 캔버스 색 반투명 배경과 블러만 남긴다. */}
        <header className="sticky top-0 z-10 flex h-topbar items-center justify-between gap-3 bg-canvas/80 px-6 backdrop-blur">
          <h1 className="text-sm font-semibold tracking-tight text-ink">
            {current?.label ?? '백오피스'}
          </h1>
          <EnvBadge />
        </header>

        {/* 본문 폭은 <b>여기서만</b> 정한다.
         *
         * 화면마다 max-w 를 따로 적던 것을 걷어냈다. 홈은 1500, 폼은 1100, 목록은
         * 무제한이라 메뉴를 옮길 때마다 콘텐츠가 좌우로 튀었다. 카드 네 칸도 화면마다
         * 폭이 달라 같은 컴포넌트가 다르게 보였다.
         *
         * 1320px 은 레일을 뺀 본문 폭이다(레일 240px 은 이 div 의 padding 바깥에 있다).
         * 카드 네 칸을 늘어놓으면 한 칸이 약 315px 로, 라벨과 두세 자리 숫자가 한 줄에
         * 들어가면서 안이 비어 보이지 않는 크기다. 1600px 에서는 한 칸이 385px 이라
         * 같은 내용에 여백만 늘어 카드가 헐거워 보였다.
         *
         * 열이 많은 표는 카드 안에서 가로 스크롤한다. 그 한 화면 때문에 나머지
         * 전부를 넓히지 않는다.
         *
         * 상하 여백을 좌우보다 조금 크게 둔다. 상단 바 바로 아래에 제목이 붙으면
         * 두 줄이 한 덩어리로 읽혀 위계가 무너진다. */}
        <main className="mx-auto w-full max-w-[1320px] px-6 pb-10 pt-6">
          {/* 화면 코드를 라우트 단위로 나눠 받으므로 경계가 필요하다. 레일과 상단 바
            * 바깥이 아니라 여기 두는 이유는, 화면을 옮길 때 네비게이션까지 사라졌다
            * 다시 나타나면 어디에 있는지를 매번 다시 찾게 되기 때문이다. */}
          <Suspense fallback={<Skeleton className="h-64" />}>
            <Outlet />
          </Suspense>
        </main>
      </div>
    </div>
  );
}

function Rail() {
  return (
    <nav
      aria-label="주 메뉴"
      // 화면에 붙은 세로 슬래브가 아니라 캔버스 위에 떠 있는 패널이다.
      // 가장자리에서 12px 떼어 두면 레일도 카드와 같은 종류의 물체로 읽혀서,
      // 화면 전체가 한 장의 문서가 아니라 위젯이 놓인 판처럼 보인다.
      className="fixed inset-y-rail-inset left-rail-inset z-20 flex w-rail flex-col overflow-hidden rounded-lg border border-rail-line bg-rail shadow-card"
    >
      {/* 워드마크만 둔다. 캐릭터까지 붙이면 좁은 레일 머리에 그래픽이 두 개가 되어 번잡하다.
       * 캐릭터는 파비콘으로만 쓴다. 탭에서는 정사각형 마크가 필요하기 때문이다.
       *
       * 구분선을 뺐다. 레일이 떠 있는 패널이 되면서 "화면을 한 줄로 가로지르는 선"이
       * 애초에 성립하지 않는다. 패널 안에 선을 그으면 그 패널만 두 칸으로 쪼개 보인다. */}
      <div className="flex h-topbar shrink-0 items-center gap-2.5 px-4">
        <img src="/brand/logo.svg" alt="ZZOL" className="h-[18px]" />
        <span className="text-2xs font-medium tracking-wide text-ink-muted">백오피스</span>
      </div>

      <div className="flex-1 overflow-y-auto px-3 pb-4">
        {NAV.map((group) => (
          <div key={group.heading} className="mt-5 first:mt-0">
            <p className="px-2 pb-2 text-2xs font-semibold tracking-wider text-rail-ink-heading">
              {group.heading}
            </p>
            <ul className="flex flex-col gap-0.5">
              {group.items.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.to === '/'}
                    className={({ isActive }) =>
                      cn(
                        // 액티브는 둥근 틴트 한 겹으로 끝낸다. 왼쪽 세로 막대까지 겹치면
                        // 표시가 두 개가 되어 오히려 지저분해진다.
                        'flex h-8 items-center gap-2.5 rounded-md px-2.5 text-sm transition-colors',
                        isActive
                          ? 'bg-rail-active font-semibold text-rail-ink-active'
                          : 'text-rail-ink hover:bg-rail-hover hover:text-ink',
                      )
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <item.icon
                          className={cn(
                            'size-4 shrink-0',
                            isActive ? 'text-accent' : 'text-ink-muted',
                          )}
                          aria-hidden
                        />
                        {item.label}
                      </>
                    )}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <AccountFooter />
    </nav>
  );
}

/**
 * 레일 바닥의 계정 영역. 지금 누구로 들어와 있는지가 늘 보여야 한다.
 * 관리자 조치는 전부 감사 로그에 이 이메일로 남으므로, 남의 계정으로 눌렀다는 것을
 * 나중에 알게 되는 상황을 만들면 안 된다.
 */
function AccountFooter() {
  const auth = useAuth();
  if (auth.status !== 'authenticated') {
    return null;
  }

  return (
    <div className="shrink-0 border-t border-rail-line p-3">
      <div className="flex items-center gap-2">
        <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-subtle text-2xs font-semibold text-ink-secondary">
          {auth.email.slice(0, 1).toUpperCase()}
        </span>
        <span className="min-w-0 flex-1 truncate text-xs text-ink-secondary" title={auth.email}>
          {auth.email}
        </span>
        <button
          type="button"
          onClick={auth.logout}
          aria-label="로그아웃"
          className="flex size-7 shrink-0 items-center justify-center rounded-md text-ink-muted transition-colors hover:bg-rail-hover hover:text-ink"
        >
          <LogOut className="size-3.5" aria-hidden />
        </button>
      </div>
    </div>
  );
}
