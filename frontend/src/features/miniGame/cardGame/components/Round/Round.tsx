import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline4 from '@/components/@common/Headline4/Headline4';
import { colorList } from '@/constants/color';
import Layout from '@/layouts/Layout';
import { Card, CardInfo, SelectedCardInfo } from '@/types/miniGame/cardGame';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';
import CircularProgress from '../CircularProgress/CircularProgress';
import * as S from './Round.styled';
import { ROUND_MAP, RoundType } from '@/types/miniGame/round';

type Props = {
  round: RoundType;
  roundTotalTime: number;
  onClickCard: (cardIndex: number) => void;
  selectedCardInfo: SelectedCardInfo;
  currentTime: number;
  isTimerActive: boolean;
  cardInfos: CardInfo[];
};

const Round = ({
  round,
  roundTotalTime,
  onClickCard,
  selectedCardInfo,
  currentTime,
  isTimerActive,
  cardInfos,
}: Props) => {
  return (
    <Layout>
      <Layout.TopBar center={<Headline4>랜덤카드 게임</Headline4>} />
      <Layout.Content>
        <S.TitleContainer>
          <S.TitleWrapper>
            <Headline2>Round {ROUND_MAP[round]}</Headline2>
            <Description>카드를 골라주세요!</Description>
          </S.TitleWrapper>
          <S.CircularProgressWrapper>
            <CircularProgress
              current={currentTime}
              total={roundTotalTime}
              isActive={isTimerActive}
            />
          </S.CircularProgressWrapper>
        </S.TitleContainer>
        <S.MyCardContainer>
          {selectedCardInfo['FIRST'].isSelected ? (
            <CardFront
              size="medium"
              card={
                {
                  type: selectedCardInfo['FIRST'].type,
                  value: selectedCardInfo['FIRST'].value,
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
                  type: selectedCardInfo['SECOND'].type,
                  value: selectedCardInfo['SECOND'].value,
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
                    type: cardInfo.cardType,
                    value: cardInfo.value,
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
