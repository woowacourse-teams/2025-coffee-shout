import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline4 from '@/components/@common/Headline4/Headline4';
import CardBack from '@/features/miniGame/cardGame/components/CardBack/CardBack';
import CircularProgress from '@/features/miniGame/cardGame/components/CircularProgress/CircularProgress';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './CardGamePlayPage.styled';
import CardFront from '../components/CardFront/CardFront';
import { Card, CardType, CardValue } from '../constants/cards';

const TOTAL_COUNT = 5;
const CARD_GRID_SIZE = 9;

// TODO: 게임 종류에 따라서 분기처리 되도록 수정 (이전 페이지에서 입력된 미니게임 종류를 토대로 화면이 바뀌어야 함 - 미니게임 종류에 대하여 Context로 관리 필요)
// TODO: 라운드가 총 2개이므로 2개의 라운드에 맞춰 이동 루트 추가
// TODO: 카드를 하나 선택했을 때 다음 페이지로 이동할 수 있도록 수정 (당장은 싱글 플레이이므로 이 로직 자체도 추후 수정되어야 함)

type RoundKey = 'round1' | 'round2';

type SelectedCardInfo = Record<
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

  const [current, setCurrent] = useState(TOTAL_COUNT);
  const [currentRound, setCurrentRound] = useState<RoundKey>('round1');
  const [selectedCardInfo, setSelectedCardInfo] = useState<SelectedCardInfo>({
    round1: {
      index: -1,
      type: null,
      value: null,
    },
    round2: {
      index: -1,
      type: null,
      value: null,
    },
  });

  useEffect(() => {
    if (current > 0) {
      const timer = setTimeout(() => setCurrent(current - 1), 1000);
      return () => clearTimeout(timer);
    } else if (current === 0) {
      // navigate(`/room/${roomId}/${miniGameId}/result`);
    }
  }, [current, miniGameId, navigate, roomId]);

  const handleCardClick = (cardIndex: number) => {
    if (currentRound === 'round1') {
      setSelectedCardInfo((prev) => ({
        ...prev,
        round1: {
          index: cardIndex,
          type: mockCardInfoMessages[cardIndex].cardType,
          value: mockCardInfoMessages[cardIndex].value,
        },
      }));
    } else if (currentRound === 'round2') {
      setSelectedCardInfo((prev) => ({
        ...prev,
        round2: {
          index: cardIndex,
          type: mockCardInfoMessages[cardIndex].cardType,
          value: mockCardInfoMessages[cardIndex].value,
        },
      }));
    }
  };

  return (
    <Layout>
      {/* game content or next game transition if/else */}
      <Layout.TopBar center={<Headline4>랜덤카드 게임</Headline4>} />
      <Layout.Content>
        <S.TitleContainer>
          <S.TitleWrapper>
            <Headline2>Round1</Headline2>
            <Description>카드를 골라주세요!</Description>
          </S.TitleWrapper>
          <S.CircularProgressWrapper>
            <CircularProgress current={current} total={TOTAL_COUNT} />
          </S.CircularProgressWrapper>
        </S.TitleContainer>
        <S.MyCardContainer>
          {selectedCardInfo[currentRound].index !== -1 ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo[currentRound].type as CardType,
                  value: selectedCardInfo[currentRound].value as CardValue,
                } as Card
              }
              onClick={() => {}}
            />
          ) : (
            <CardBack size="medium" />
          )}
          <CardBack size="medium" onClick={() => {}} />
        </S.MyCardContainer>
        <S.CardContainer>
          {mockCardInfoMessages.map((_, index) => {
            return selectedCardInfo[currentRound].index === index ? (
              <CardFront
                onClick={() => {}}
                card={
                  {
                    type: mockCardInfoMessages[index].cardType as CardType,
                    value: mockCardInfoMessages[index].value as CardValue,
                  } as Card
                }
              />
            ) : (
              <CardBack key={index} onClick={() => handleCardClick(index)} />
            );
          })}
        </S.CardContainer>
      </Layout.Content>
    </Layout>
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
