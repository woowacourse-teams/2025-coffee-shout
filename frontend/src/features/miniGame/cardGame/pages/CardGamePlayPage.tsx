import { useEffect, useState } from 'react';
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
  const { isTransition, currentRound, currentCardGameState } = useCardGame();
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

  const handleCardClick = (cardIndex: number) => {
    if (currentRound === 1) {
      setSelectedCardInfo((prev) => ({
        ...prev,
        1: {
          index: cardIndex,
          type: mockCardInfoMessages[cardIndex].cardType,
          value: mockCardInfoMessages[cardIndex].value,
        },
      }));

      return;
    }

    if (currentRound === 2) {
      setSelectedCardInfo((prev) => ({
        ...prev,
        2: {
          index: cardIndex,
          type: mockCardInfoMessages[cardIndex].cardType,
          value: mockCardInfoMessages[cardIndex].value,
        },
      }));

      setTimeout(() => {
        navigate(`/room/${joinCode}/${miniGameType}/result`);
      }, 2000);
    }
  };

  useEffect(() => {
    if (currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime(currentTime - 1), 1000);
      return () => clearTimeout(timer);
    } else if (currentCardGameState === 'LOADING' && currentRound === 2) {
    } else if (currentTime === 0 && currentRound === 2 && currentCardGameState === 'PLAYING') {
      setCurrentTime(TOTAL_COUNT);
      // navigate(`/room/${joinCode}/${miniGameType}/result`);
    }
  }, [currentTime, navigate, joinCode, miniGameType, currentRound, currentCardGameState]);

  return isTransition ? (
    <MiniGameTransition currentRound={currentRound} />
  ) : (
    <Round
      key={currentRound}
      round={currentRound}
      onClickCard={handleCardClick}
      selectedCardInfo={selectedCardInfo}
      currentTime={currentTime}
    />
  );
};

export default CardGamePlayPage;

const mockCardInfoMessages = [
  {
    cardType: 'ADDITION',
    value: 10,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'ADDITION',
    value: 30,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'ADDITION',
    value: -10,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'ADDITION',
    value: -20,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'ADDITION',
    value: 40,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'MULTIPLIER',
    value: 2,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'MULTIPLIER',
    value: 0,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'MULTIPLIER',
    value: -1,
    selected: false,
    playerName: null,
  },
  {
    cardType: 'ADDITION',
    value: -40,
    selected: false,
    playerName: null,
  },
];
