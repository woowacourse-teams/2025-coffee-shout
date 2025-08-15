import { CardGameState, CardInfo, SelectedCardInfo } from '@/types/miniGame';
import { CardGameRound } from '@/types/round';
import { createContext, Dispatch, SetStateAction, useContext } from 'react';

type CardGameContextType = {
  isTransition: boolean;
  currentRound: CardGameRound;
  currentCardGameState: CardGameState;
  cardInfos: CardInfo[];
  selectedCardInfo: SelectedCardInfo;
  setSelectedCardInfo: Dispatch<SetStateAction<SelectedCardInfo>>;
};

export const CardGameContext = createContext<CardGameContextType | null>(null);

export const useCardGame = () => {
  const context = useContext(CardGameContext);
  if (!context) {
    throw new Error('useCardGame는 CardGameProvider 안에서 사용해야 합니다.');
  }
  return context;
};
