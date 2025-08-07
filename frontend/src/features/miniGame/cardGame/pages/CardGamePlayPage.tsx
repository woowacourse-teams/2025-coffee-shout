import { useEffect, useRef, useState } from 'react';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import Round from '../components/Round/Round';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { TOTAL_COUNT } from '@/types/round';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';

const CardGamePlayPage = () => {
  const { myName, joinCode } = useIdentifier();
  const {
    isTransition,
    currentRound,
    currentCardGameState,
    cardInfos,
    selectedCardInfo,
    setSelectedCardInfo,
  } = useCardGame();
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
  const { send } = useWebSocket();

  const isTimerReset = useRef(false);

  const handleCardClick = (cardIndex: number) => {
    if (selectedCardInfo[currentRound].isSelected) {
      return;
    }

    setSelectedCardInfo((prev) => ({
      ...prev,
      [currentRound]: {
        isSelected: true,
        type: cardInfos[cardIndex].cardType,
        value: cardInfos[cardIndex].value,
      },
    }));

    send(`/room/${joinCode}/minigame/command`, {
      commandType: 'SELECT_CARD',
      commandRequest: {
        playerName: myName,
        cardIndex,
      },
    });
  };

  useEffect(() => {
    if (currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime((prev) => prev - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [currentTime]);

  useEffect(() => {
    if (currentRound === 'SECOND' && currentCardGameState === 'PLAYING' && !isTimerReset.current) {
      setCurrentTime(TOTAL_COUNT);
      isTimerReset.current = true;
    }
  }, [currentRound, currentCardGameState]);

  return isTransition ? (
    <MiniGameTransition currentRound={currentRound} />
  ) : (
    <Round
      key={currentRound}
      round={currentRound}
      onClickCard={handleCardClick}
      selectedCardInfo={selectedCardInfo}
      currentTime={currentTime}
      cardInfos={cardInfos}
    />
  );
};

export default CardGamePlayPage;
