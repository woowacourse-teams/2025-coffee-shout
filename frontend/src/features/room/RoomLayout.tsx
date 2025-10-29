import { useBackButtonConfirm } from '@/hooks/useBackButtonConfirm';
import { useNavigationGuard } from '@/hooks/useNavigateGuard';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import { useRoomAccessGuard } from '@/hooks/useRoomAccessGuard';
import { Outlet } from 'react-router-dom';

const RoomLayout = () => {
  useNavigationGuard();
  useRoomAccessGuard();

  const navigate = useReplaceNavigate();
  useBackButtonConfirm({
    onConfirm: () => navigate('/'),
    message: '정말 방에서 나가시겠습니까?\n방에서 나가도 게임은 계속 진행됩니다.',
  });

  return <Outlet />;
};

export default RoomLayout;
