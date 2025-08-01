import { useEffect, useState } from 'react';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import Round from '../components/Round/Round';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { RoundKey, TOTAL_COUNT } from '@/types/round';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';

export type SelectedCardInfo = Record<
  RoundKey,
  {
    index: number;
    type: string | null;
    value: number | null;
  }
>;

const CardGamePlayPage = () => {
  const { myName, joinCode } = useIdentifier();
  const { isTransition, currentRound, currentCardGameState, cardInfos } = useCardGame();
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
  const { send } = useWebSocket();

  const [selectedCardInfo, setSelectedCardInfo] = useState<SelectedCardInfo>({
    1: {
      index: -1,
      type: null,
      value: null,
    },
    2: {
      index: -1,
      type: null,
      value: null,
    },
  });

  const handleCardClick = (cardIndex: number) => {
    if (currentRound === 1) {
      if (selectedCardInfo[1].index !== -1) {
        return;
      }

      setSelectedCardInfo((prev) => ({
        ...prev,
        1: {
          index: cardIndex,
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

      return;
    }

    if (currentRound === 2) {
      if (selectedCardInfo[2].index !== -1) {
        return;
      }

      setSelectedCardInfo((prev) => ({
        ...prev,
        2: {
          index: cardIndex,
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
    }
  };

  useEffect(() => {
    if (currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime((prev) => prev - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [currentTime]);

  useEffect(() => {
    if (currentTime === 0 && currentRound === 2 && currentCardGameState === 'PLAYING') {
      setCurrentTime(TOTAL_COUNT);
    }
  }, [currentTime, currentRound, currentCardGameState]);

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
