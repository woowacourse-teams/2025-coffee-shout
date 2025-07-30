import { createContext, useContext } from 'react';
import { PlayerType } from '@/types/player';

type PlayerRoleContextType = {
  playerRole: PlayerType | null;
  setGuest: () => void;
  setHost: () => void;
};

export const PlayerRoleContext = createContext<PlayerRoleContextType | null>(null);

export const usePlayerRole = () => {
  const context = useContext(PlayerRoleContext);
  if (!context) {
    throw new Error('usePlayerRole 는 PlayerRoleProvider 안에서 사용해야 합니다.');
  }
  return context;
};
