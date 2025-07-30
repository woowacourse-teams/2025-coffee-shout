import { PlayerType } from '@/types/player';
import { PlayerTypeContext } from './PlayerTypeContext';
import { PropsWithChildren, useState } from 'react';

export const PlayerTypeProvider = ({ children }: PropsWithChildren) => {
  const [playerType, setPlayerType] = useState<PlayerType | null>(null);

  const setGuest = () => {
    setPlayerType('GUEST');
  };

  const setHost = () => {
    setPlayerType('HOST');
  };
  return (
    <PlayerTypeContext.Provider value={{ playerType, setGuest, setHost }}>
      {children}
    </PlayerTypeContext.Provider>
  );
};
