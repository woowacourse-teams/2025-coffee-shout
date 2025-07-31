import { useEffect, useState } from 'react';
import MiniGameTransition from '@/features/miniGame/components/MiniGameTransition/MiniGameTransition';
import { RoundKey } from '@/types/round';
import { useNavigate, useParams } from 'react-router-dom';
import Round from '../components/Round/Round';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

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
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
  const [isTransition, setIsTransition] = useState(false);
  const [currentRound, setCurrentRound] = useState<RoundKey>(1);
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

      setTimeout(() => {
        setIsTransition(true);
      }, 2000);

      setTimeout(() => {
        setIsTransition(false);
        setCurrentRound(2);
      }, 4000);

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
    } else if (currentTime === 0) {
      navigate(`/room/${joinCode}/${miniGameType}/result`);
    }
  }, [currentTime, navigate, joinCode, miniGameType]);

  return isTransition ? (
    <MiniGameTransition prevRound={currentRound} />
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
