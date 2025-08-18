import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline4 from '@/components/@common/Headline4/Headline4';
import { colorList } from '@/constants/color';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import Layout from '@/layouts/Layout';
import { Card, CardInfo, SelectedCardInfo } from '@/types/miniGame/cardGame';
import { ROUND_MAP, RoundType } from '@/types/miniGame/round';
import CardBack from '../CardBack/CardBack';
import CardFront from '../CardFront/CardFront';
import CircularProgress from '../CircularProgress/CircularProgress';
import * as S from './Round.styled';

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
  const isCardSelectedInCurrentRound = selectedCardInfo[round].isSelected;
  const { getParticipantColorIndex } = useParticipants();

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

        <S.MyCardContainer data-testid="selected-card-area">
          {selectedCardInfo['FIRST'].isSelected ? (
            <CardFront
              size="medium"
              data-testid="card-selected-round-1"
              card={
                {
                  type: selectedCardInfo['FIRST'].type,
                  value: selectedCardInfo['FIRST'].value,
                } as Card
              }
            />
          ) : (
            <CardBack size="medium" disabled={true} data-testid="card-empty-round-1" />
          )}
          {selectedCardInfo['SECOND'].isSelected ? (
            <CardFront
              size="medium"
              data-testid="card-selected-round-2"
              card={
                {
                  type: selectedCardInfo['SECOND'].type,
                  value: selectedCardInfo['SECOND'].value,
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

            let playerColor = null;
            if (cardInfo.playerName !== null) {
              playerColor = colorList[getParticipantColorIndex(cardInfo.playerName)];
            }

            return isThisCardSelected ? (
              <CardFront
                key={index}
                data-testid={`card-${index}`}
                data-flipped="true"
                data-selected="true"
                card={
                  {
                    type: cardInfo.cardType,
                    value: cardInfo.value,
                  } as Card
                }
                playerColor={playerColor}
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
