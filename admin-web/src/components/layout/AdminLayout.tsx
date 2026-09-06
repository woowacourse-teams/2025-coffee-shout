import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  AlertTriangle,
  Gamepad2,
  LayoutDashboard,
  MessageSquareWarning,
  Palette,
  ScrollText,
  Search,
  ShieldBan,
  SpellCheck,
  Users,
  UserCog,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { cn } from '@/lib/cn';
import { EnvBadge } from '@/components/ui/EnvBadge';

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
      { to: '/design', label: '디자인', icon: Palette },
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

      <div className="pl-rail">
        <header className="sticky top-0 z-10 flex h-topbar items-center justify-between gap-3 border-b border-border-default bg-surface px-5">
          <h1 className="text-sm font-semibold text-ink">{current?.label ?? '백오피스'}</h1>
          <div className="flex items-center gap-2">
            <EnvBadge />
          </div>
        </header>

        {/* 본문에 최대폭을 두지 않는다. 표는 넓을수록 한 화면에 더 담긴다. */}
        <main className="p-5">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function Rail() {
  return (
    <nav
      aria-label="주 메뉴"
      className="fixed inset-y-0 left-0 z-20 flex w-rail flex-col border-r border-rail-line bg-rail"
    >
      {/* 로고는 원래 코랄 그대로 올린다. 다크 레일 위 대비가 6.38:1 이라
       * 화이트 버전을 따로 만들 필요가 없다. */}
      <div className="flex h-topbar shrink-0 items-center gap-2 border-b border-rail-line px-4">
        <img src="/brand/character.svg" alt="" className="size-6" aria-hidden />
        <img src="/brand/logo.svg" alt="ZZOL" className="h-3.5" />
        <span className="text-2xs font-medium text-white/50">백오피스</span>
      </div>

      <div className="flex-1 overflow-y-auto py-3">
        {NAV.map((group) => (
          <div key={group.heading} className="mb-4 px-2">
            <p className="px-2 pb-1 text-2xs font-medium tracking-wide text-white/35">
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
                        'relative flex items-center gap-2.5 rounded px-2 py-1.5 text-sm transition-colors',
                        isActive
                          ? 'bg-rail-active font-medium text-white'
                          : 'text-white/70 hover:bg-white/5 hover:text-white',
                      )
                    }
                  >
                    {({ isActive }) => (
                      <>
                        {/* 액티브 인디케이터. 브랜드 코랄이 유일하게 선으로 등장하는 자리다. */}
                        {isActive && (
                          <span
                            className="absolute inset-y-1 left-0 w-0.5 rounded-full bg-accent"
                            aria-hidden
                          />
                        )}
                        <item.icon className="size-4 shrink-0" aria-hidden />
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
    </nav>
  );
}
