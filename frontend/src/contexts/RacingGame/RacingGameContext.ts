import { createContext, useContext } from 'react';

type RacingGameContextType = {
  racingGameState: 'DESCRIPTION' | 'PREPARE' | 'PLAYING' | 'DONE';
};

export const RacingGameContext = createContext<RacingGameContextType | null>(null);

export const useRacingGame = () => {
  const context = useContext(RacingGameContext);
  if (!context) {
    throw new Error('useRacingGame는 RacingGameProvider 안에서 사용해야 합니다.');
  }
  return context;
};
