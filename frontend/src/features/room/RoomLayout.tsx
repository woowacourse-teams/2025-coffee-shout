import { useBackButtonConfirm } from '@/hooks/useBackButtonConfirm';
import { useNavigationGuard } from '@/hooks/useNavigateGuard';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import { useRoomAccessGuard } from '@/hooks/useRoomAccessGuard';
import { Outlet, useLocation } from 'react-router-dom';

const RoomLayout = () => {
  useNavigationGuard();
  useRoomAccessGuard();

  const location = useLocation();
  const navigate = useReplaceNavigate();

  const isRouletteOrOrderPath = /(roulette|order)/.test(location.pathname);

  useBackButtonConfirm({
    onConfirm: () => navigate('/'),
    message: isRouletteOrOrderPath
      ? '정말 방에서 나가시겠습니까?'
      : '게임 방에서 나가시겠습니까?\n방에서 나가도 게임은 계속 진행됩니다.',
  });

  return <Outlet />;
};

export default RoomLayout;
