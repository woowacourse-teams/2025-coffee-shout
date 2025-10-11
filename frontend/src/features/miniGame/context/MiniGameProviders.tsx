import { MiniGameType } from '@/types/miniGame/common';
import { PropsWithChildren } from 'react';
import { useParams } from 'react-router-dom';
import { GAME_PROVIDER } from '../config/gameProviderConfigs';

const MiniGameProviders = ({ children }: PropsWithChildren) => {
  const { miniGameType } = useParams();

  if (!miniGameType || !(miniGameType in GAME_PROVIDER)) {
    return <>{children}</>;
  }

  const ProviderComponent = GAME_PROVIDER[miniGameType as MiniGameType];

  if (!ProviderComponent) {
    return <>{children}</>;
  }

  return <ProviderComponent>{children}</ProviderComponent>;
};

export default MiniGameProviders;
