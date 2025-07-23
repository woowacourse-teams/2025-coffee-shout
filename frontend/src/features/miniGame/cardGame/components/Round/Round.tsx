import Layout from '@/layouts/Layout';
import * as S from './Round.styled';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';
import CircularProgress from '../CircularProgress/CircularProgress';
import CardBack from '../CardBack/CardBack';
import { Card, CardType, CardValue } from '../../constants/cards';
import CardFront from '../CardFront/CardFront';
import { RoundKey } from '@/types/round';
import { SelectedCardInfo } from '../../pages/CardGamePlayPage';

const TOTAL_COUNT = 10;

type Props = {
  round: RoundKey;
  onClickCard: (cardIndex: number) => void;
  selectedCardInfo: SelectedCardInfo;
  currentTime: number;
};

const Round = ({ round, onClickCard, selectedCardInfo, currentTime }: Props) => {
  return (
    <Layout>
      <Layout.TopBar center={<Headline4>랜덤카드 게임</Headline4>} />
      <Layout.Content>
        <S.TitleContainer>
          <S.TitleWrapper>
            <Headline2>Round {round}</Headline2>
            <Description>카드를 골라주세요!</Description>
          </S.TitleWrapper>
          <S.CircularProgressWrapper>
            <CircularProgress current={currentTime} total={TOTAL_COUNT} />
          </S.CircularProgressWrapper>
        </S.TitleContainer>
        <S.MyCardContainer>
          {selectedCardInfo[round].index !== -1 ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo[round].type as CardType,
                  value: selectedCardInfo[round].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" />
          )}
          <CardBack size="medium" />
        </S.MyCardContainer>
        <S.CardContainer>
          {mockCardInfoMessages.map((_, index) => {
            return selectedCardInfo[round].index === index ? (
              <CardFront
                card={
                  {
                    type: mockCardInfoMessages[index].cardType as CardType,
                    value: mockCardInfoMessages[index].value as CardValue,
                  } as Card
                }
              />
            ) : (
              <CardBack key={index} onClick={() => onClickCard(index)} />
            );
          })}
        </S.CardContainer>
      </Layout.Content>
    </Layout>
  );
};

export default Round;

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
