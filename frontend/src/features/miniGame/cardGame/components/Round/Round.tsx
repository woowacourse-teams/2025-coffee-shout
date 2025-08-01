import Layout from '@/layouts/Layout';
import * as S from './Round.styled';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';
import CircularProgress from '../CircularProgress/CircularProgress';
import CardBack from '../CardBack/CardBack';
import { Card, CardType, CardValue } from '../../constants/cards';
import CardFront from '../CardFront/CardFront';
import { SelectedCardInfo } from '../../pages/CardGamePlayPage';
import { RoundKey, TOTAL_COUNT } from '@/types/round';
import { CardInfo } from '@/types/miniGame';

type Props = {
  round: RoundKey;
  onClickCard: (cardIndex: number) => void;
  selectedCardInfo: SelectedCardInfo;
  currentTime: number;
  cardInfos: CardInfo[];
};

const Round = ({ round, onClickCard, selectedCardInfo, currentTime, cardInfos }: Props) => {
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
          {selectedCardInfo[1].index !== -1 ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo[1].type as CardType,
                  value: selectedCardInfo[1].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} />
          )}
          {selectedCardInfo[2].index !== -1 ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo[2].type as CardType,
                  value: selectedCardInfo[2].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} />
          )}
        </S.MyCardContainer>
        <S.CardContainer>
          {cardInfos.map((_, index) => {
            return selectedCardInfo[round].index === index ? (
              <CardFront
                card={
                  {
                    type: cardInfos[index].cardType as CardType,
                    value: cardInfos[index].value as CardValue,
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
