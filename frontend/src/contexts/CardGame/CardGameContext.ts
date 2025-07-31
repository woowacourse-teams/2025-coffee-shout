import { CardGameState, CardInfo } from '@/types/miniGame';
import { RoundKey } from '@/types/round';
import { createContext, useContext } from 'react';

type CardGameContextType = {
  startCardGame: boolean;
  isTransition: boolean;
  currentRound: RoundKey;
  currentCardGameState: CardGameState;
  cardInfos: CardInfo[];
};

export const CardGameContext = createContext<CardGameContextType | null>(null);

export const useCardGame = () => {
  const context = useContext(CardGameContext);
  if (!context) {
    throw new Error('useCardGame는 CardGameProvider 안에서 사용해야 합니다.');
  }
  return context;
};
