import { useBackButtonConfirm } from '@/hooks/useBackButtonConfirm';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import { MiniGameType } from '@/types/miniGame/common';
import { PropsWithChildren } from 'react';
import { useParams } from 'react-router-dom';
import { GAME_CONFIGS } from '../config/gameConfigs';

const MiniGameProviders = ({ children }: PropsWithChildren) => {
  const navigate = useReplaceNavigate();
  useBackButtonConfirm({ onConfirm: () => navigate('/') });

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
