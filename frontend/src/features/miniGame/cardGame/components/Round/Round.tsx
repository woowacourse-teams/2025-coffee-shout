import Layout from '@/layouts/Layout';
import * as S from './Round.styled';
import Headline4 from '@/components/@common/Headline4/Headline4';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';
import CircularProgress from '../CircularProgress/CircularProgress';
import CardBack from '../CardBack/CardBack';
import { Card, CardType, CardValue } from '../../constants/cards';
import CardFront from '../CardFront/CardFront';
import { TOTAL_COUNT } from '@/types/round';
import { CardGameRound, CardInfo } from '@/types/miniGame';
import { colorList } from '@/constants/color';
import { SelectedCardInfo } from '@/contexts/CardGame/CardGameProvider';
import { ROUND_NUMBER_MAP } from '@/constants/miniGame';

type Props = {
  round: CardGameRound;
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
            <Headline2>Round {ROUND_NUMBER_MAP[round]}</Headline2>
            <Description>카드를 골라주세요!</Description>
          </S.TitleWrapper>
          <S.CircularProgressWrapper>
            <CircularProgress current={currentTime} total={TOTAL_COUNT} />
          </S.CircularProgressWrapper>
        </S.TitleContainer>
        <S.MyCardContainer>
          {selectedCardInfo['FIRST'].isSelected ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo['FIRST'].type as CardType,
                  value: selectedCardInfo['FIRST'].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} />
          )}
          {selectedCardInfo['SECOND'].isSelected ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo['SECOND'].type as CardType,
                  value: selectedCardInfo['SECOND'].value as CardValue,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} />
          )}
        </S.MyCardContainer>
        <S.CardContainer>
          {cardInfos.map((cardInfo, index) => {
            return cardInfo.selected ? (
              <CardFront
                card={
                  {
                    type: cardInfo.cardType as CardType,
                    value: cardInfo.value as CardValue,
                  } as Card
                }
                playerColor={colorList[cardInfo.colorIndex]}
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
