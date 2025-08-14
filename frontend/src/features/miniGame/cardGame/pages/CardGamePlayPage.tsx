import { useEffect, useRef, useState } from 'react';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import Round from '../components/Round/Round';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { TOTAL_COUNT } from '@/types/round';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import PrepareOverlay from '../components/PrepareOverlay/PrepareOverlay';

const CardGamePlayPage = () => {
  const { myName, joinCode } = useIdentifier();
  const { isTransition, currentRound, currentCardGameState, cardInfos, selectedCardInfo } =
    useCardGame();
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
  const [isTimerActive, setIsTimerActive] = useState(false);
  const { send } = useWebSocket();

  const isTimerReset = useRef(false);

  const handleCardClick = (cardIndex: number) => {
    if (selectedCardInfo[currentRound].isSelected) {
      return;
    }

    send(`/room/${joinCode}/minigame/command`, {
      commandType: 'SELECT_CARD',
      commandRequest: {
        playerName: myName,
        cardIndex,
      },
    });
  };

  useEffect(() => {
    if (isTimerActive && currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime((prev) => prev - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [currentTime, isTimerActive]);

  useEffect(() => {
    if (currentCardGameState === 'DESCRIPTION') {
      setCurrentTime(TOTAL_COUNT);
      setIsTimerActive(false);
    } else if (currentCardGameState === 'PLAYING') {
      if (currentRound === 'FIRST') {
        setCurrentTime(TOTAL_COUNT);
        setIsTimerActive(true);
        isTimerReset.current = false;
      } else if (currentRound === 'SECOND' && !isTimerReset.current) {
        setCurrentTime(TOTAL_COUNT);
        setIsTimerActive(true);
        isTimerReset.current = true;
      }
    }
  }, [currentRound, currentCardGameState]);

  if (isTransition) {
    return <MiniGameTransition currentRound={currentRound} />;
  }

  return (
    <>
      {currentCardGameState === 'DESCRIPTION' && <PrepareOverlay />}
      <Round
        key={currentRound}
        round={currentRound}
        onClickCard={handleCardClick}
        selectedCardInfo={selectedCardInfo}
        currentTime={currentTime}
        isTimerActive={isTimerActive}
        cardInfos={cardInfos}
      />
    </>
  );
};

export default CardGamePlayPage;
