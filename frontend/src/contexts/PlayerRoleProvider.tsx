import { PlayerRole } from '@/types/player';
import { PlayerRoleContext } from './PlayerRoleContext';
import { PropsWithChildren, useState } from 'react';

export const PlayerRoleProvider = ({ children }: PropsWithChildren) => {
  const [playerRole, setPlayerRole] = useState<PlayerRole | null>(null);

  const setGuest = () => {
    setPlayerRole('GUEST');
  };

  const setHost = () => {
    setPlayerRole('HOST');
  };
  return (
    <PlayerRoleContext.Provider value={{ playerRole, setGuest, setHost }}>
      {children}
    </PlayerRoleContext.Provider>
  );
};
