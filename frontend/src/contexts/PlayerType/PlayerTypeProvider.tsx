import { PlayerType } from '@/types/player';
import { PlayerTypeContext } from './PlayerTypeContext';
import { PropsWithChildren, useEffect, useState } from 'react';

const STORAGE_KEY = 'coffee-shout-player-type' as const;

export const PlayerTypeProvider = ({ children }: PropsWithChildren) => {
  const [playerType, setPlayerType] = useState<PlayerType | null>(() => {
    return sessionStorage.getItem(STORAGE_KEY) as PlayerType | null;
  });

  useEffect(() => {
    if (playerType) {
      sessionStorage.setItem(STORAGE_KEY, playerType);
    } else {
      sessionStorage.removeItem(STORAGE_KEY);
    }
  }, [playerType]);

  const setGuest = () => {
    setPlayerType('GUEST');
  };

  const setHost = () => {
    setPlayerType('HOST');
  };

  return (
    <PlayerTypeContext.Provider value={{ playerType, setPlayerType, setGuest, setHost }}>
      {children}
    </PlayerTypeContext.Provider>
  );
};
