import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import { useEffect, useRef, useState } from 'react';
import PrepareOverlay from '../components/PrepareOverlay/PrepareOverlay';
import Round from '../components/Round/Round';

const ROUND_TOTAL_TIME = 10;

const CardGamePlayPage = () => {
  const { myName, joinCode } = useIdentifier();
  const { isTransition, currentRound, currentCardGameState, cardInfos, selectedCardInfo } =
    useCardGame();
  const [currentTime, setCurrentTime] = useState(ROUND_TOTAL_TIME);
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
    if (currentCardGameState === 'PREPARE') {
      setCurrentTime(ROUND_TOTAL_TIME);
      return;
    }

    if (currentCardGameState === 'PLAYING') {
      if (currentRound === 'FIRST') {
        setCurrentTime(ROUND_TOTAL_TIME);
        setIsTimerActive(true);
        isTimerReset.current = false;
        return;
      }

      if (currentRound === 'SECOND' && !isTimerReset.current) {
        setCurrentTime(ROUND_TOTAL_TIME);
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
      {currentCardGameState === 'PREPARE' && <PrepareOverlay />}
      <Round
        key={currentRound}
        round={currentRound}
        roundTotalTime={ROUND_TOTAL_TIME}
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
