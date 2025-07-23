import { useEffect, useState } from 'react';
// import { useParams } from 'react-router-dom';
import Round from '../components/Round/Round';
import { RoundKey } from '@/types/round';
import { useNavigate, useParams } from 'react-router-dom';

// TODO: 게임 종류에 따라서 분기처리 되도록 수정 (이전 페이지에서 입력된 미니게임 종류를 토대로 화면이 바뀌어야 함 - 미니게임 종류에 대하여 Context로 관리 필요)
// TODO: 라운드가 총 2개이므로 2개의 라운드에 맞춰 이동 루트 추가
// TODO: 카드를 하나 선택했을 때 다음 페이지로 이동할 수 있도록 수정 (당장은 싱글 플레이이므로 이 로직 자체도 추후 수정되어야 함)

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
  const { roomId, miniGameId } = useParams();
  const [currentTime, setCurrentTime] = useState(TOTAL_COUNT);
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
        setCurrentRound(2);
        navigate(`/room/${roomId}/${miniGameId}/result`);
      }, 3000);

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
    }
  };

  useEffect(() => {
    if (currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime(currentTime - 1), 1000);
      return () => clearTimeout(timer);
    } else if (currentTime === 0) {
      navigate(`/room/${roomId}/${miniGameId}/result`);
    }
  }, [currentTime, navigate, roomId, miniGameId]);

  return (
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
