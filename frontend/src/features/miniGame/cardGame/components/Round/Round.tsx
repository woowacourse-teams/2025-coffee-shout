import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Layout from '@/layouts/Layout';
import { CardInfo } from '@/types/miniGame';
import { RoundKey, TOTAL_COUNT } from '@/types/round';
import { Card, CardType, CardValue } from '../../constants/cards';
import { SelectedCardInfo } from '../../pages/CardGamePlayPage';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';
import CircularProgress from '../CircularProgress/CircularProgress';
import * as S from './Round.styled';

type Props = {
  round: RoundKey;
  onClickCard: (cardIndex: number) => void;
  selectedCardInfo: SelectedCardInfo;
  currentTime: number;
  cardInfos: CardInfo[];
};

const Round = ({ round, onClickCard, selectedCardInfo, currentTime, cardInfos }: Props) => {
  const isCardSelectedInCurrentRound = selectedCardInfo[round].index !== -1;

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
        <S.MyCardContainer data-testid="selected-card-area">
          {selectedCardInfo[1].index !== -1 ? (
            <CardFront
              size="medium"
              data-testid="card-selected-round-1"
              card={
                {
                  type: selectedCardInfo[1].type as CardType,
                  value: selectedCardInfo[1].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} data-testid="card-empty-round-1" />
          )}
          {selectedCardInfo[2].index !== -1 ? (
            <CardFront
              size="medium"
              data-testid="card-selected-round-2"
              card={
                {
                  type: selectedCardInfo[2].type as CardType,
                  value: selectedCardInfo[2].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} data-testid="card-empty-round-2" />
          )}
        </S.MyCardContainer>
        <S.CardContainer>
          {cardInfos.map((cardInfo, index) => {
            const isThisCardSelected = cardInfo.selected;
            const shouldDisableCard = isCardSelectedInCurrentRound && !isThisCardSelected;

            return isThisCardSelected ? (
              <CardFront
                key={index}
                data-testid={`card-${index}`}
                data-flipped="true"
                data-selected="true"
                card={
                  {
                    type: cardInfo.cardType as CardType,
                    value: cardInfo.value as CardValue,
                  } as Card
                }
              />
            ) : (
              <CardBack
                key={index}
                data-testid={`card-${index}`}
                data-flipped="false"
                data-selected="false"
                disabled={shouldDisableCard}
                onClick={() => onClickCard(index)}
              />
            );
          })}
        </S.CardContainer>
      </Layout.Content>
    </Layout>
  );
};

export default Round;
