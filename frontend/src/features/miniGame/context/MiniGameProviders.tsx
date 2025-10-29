import { useBackButtonConfirm } from '@/hooks/useBackButtonConfirm';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import { MiniGameType } from '@/types/miniGame/common';
import { PropsWithChildren } from 'react';
import { useParams } from 'react-router-dom';
import { GAME_CONFIGS } from '../config/gameConfigs';

const MiniGameProviders = ({ children }: PropsWithChildren) => {
  const navigate = useReplaceNavigate();
  useBackButtonConfirm({
    onConfirm: () => navigate('/'),
    message: '게임 방에서 나가시겠습니까?\n방에서 나가져도 게임이 계속 진행됩니다.',
  });

  const { miniGameType } = useParams();
  if (!miniGameType || !(miniGameType in GAME_CONFIGS)) {
    return <>{children}</>;
  }

  const ProviderComponent = GAME_CONFIGS[miniGameType as MiniGameType].Provider;
  if (!ProviderComponent) {
    return <>{children}</>;
  }

  return <ProviderComponent>{children}</ProviderComponent>;
};

export default MiniGameProviders;
