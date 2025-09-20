import { CardGameState, CardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';
import { useState } from 'react';

export const useCardGameState = () => {
  const [isTransition, setIsTransition] = useState<boolean>(false);
  const [currentRound, setCurrentRound] = useState<RoundType>('FIRST');
  const [currentCardGameState, setCurrentCardGameState] = useState<CardGameState>('READY');
  const [cardInfos, setCardInfos] = useState<CardInfo[]>([]);

  const updateCardGameState = (state: CardGameState) => {
    setCurrentCardGameState(state);
  };

  const updateCardInfos = (cardInfoMessages: CardInfo[]) => {
    setCardInfos(cardInfoMessages);
  };

  const updateCurrentRound = (round: RoundType) => {
    setCurrentRound(round);
  };

  const updateTransition = (transition: boolean) => {
    setIsTransition(transition);
  };

  return {
    isTransition,
    currentRound,
    currentCardGameState,
    cardInfos,
    updateCardGameState,
    updateCardInfos,
    updateCurrentRound,
    updateTransition,
  };
};
