import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { PropsWithChildren } from 'react';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameContext } from './CardGameContext';
import { useCardGameState } from './hooks/useCardGameState';
import { useSelectedCard } from './hooks/useSelectedCard';
import { useCardGameHandlers } from './hooks/useCardGameHandlers';

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const { joinCode, myName } = useIdentifier();

  const {
    isTransition,
    currentRound,
    currentCardGameState,
    cardInfos,
    updateTransition,
    updateCurrentRound,
    updateCardGameState,
    updateCardInfos,
  } = useCardGameState();

  const { selectedCardInfo, setSelectedCardInfo, updateSelectedCardInfo } = useSelectedCard(myName);

  const { handleCardGameState } = useCardGameHandlers({
    updateCardGameState,
    updateCardInfos,
    updateCurrentRound,
    updateTransition,
    updateSelectedCardInfo,
  });

  useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);

  return (
    <CardGameContext.Provider
      value={{
        isTransition,
        currentRound,
        currentCardGameState,
        cardInfos,
        selectedCardInfo,
        setSelectedCardInfo,
      }}
    >
      {children}
    </CardGameContext.Provider>
  );
};

export default CardGameProvider;
