import { useEffect, useRef, useState } from 'react';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import { useNavigate, useParams } from 'react-router-dom';
import Round from '../components/Round/Round';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { RoundKey } from '@/types/round';

const TOTAL_COUNT = 10;

export type SelectedCardInfo = Record<
  RoundKey,
  {
    index: number;
    type: string | null;
    value: number | null;
  }
>;

const CardGamePlayPage = () => {
  const navigate = useNavigate();

  const { miniGameType } = useParams();
  const { joinCode } = useIdentifier();
  const { isTransition, currentRound, currentCardGameState, cardInfos } = useCardGame();
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
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
  const hasResetTimerInRound2 = useRef(false);

  const handleCardClick = (cardIndex: number) => {
    if (currentRound === 1) {
      setSelectedCardInfo((prev) => ({
        ...prev,
        1: {
          index: cardIndex,
          type: cardInfos[cardIndex].cardType,
          value: cardInfos[cardIndex].value,
        },
      }));

      return;
    }

    if (currentRound === 2) {
      setSelectedCardInfo((prev) => ({
        ...prev,
        2: {
          index: cardIndex,
          type: cardInfos[cardIndex].cardType,
          value: cardInfos[cardIndex].value,
        },
      }));

      // setTimeout(() => {
      //   navigate(`/room/${joinCode}/${miniGameType}/result`);
      // }, 2000);
    }
  };

  const shouldResetTimer =
    currentTime === 0 &&
    currentRound === 2 &&
    currentCardGameState === 'PLAYING' &&
    !hasResetTimerInRound2.current;

  useEffect(() => {
    if (currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime(currentTime - 1), 1000);
      return () => clearTimeout(timer);
    } else if (shouldResetTimer) {
      setCurrentTime(TOTAL_COUNT);
      hasResetTimerInRound2.current = true;
    }
  }, [
    currentTime,
    navigate,
    joinCode,
    miniGameType,
    currentRound,
    currentCardGameState,
    cardInfos,
    shouldResetTimer,
  ]);

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
