import Headline4 from '@/components/@common/Headline4/Headline4';
import Layout from '@/layouts/Layout';
import { CardInfo, SelectedCardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';
import GameCardGrid from './GameCardGrid';
import PlayerCardDisplay from './PlayerCardDisplay';
import * as S from './Round.styled';
import RoundHeader from './RoundHeader';

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
        <RoundHeader
          round={round}
          currentTime={currentTime}
          roundTotalTime={roundTotalTime}
          isTimerActive={isTimerActive}
        />
        <S.MyCardContainer>
          <PlayerCardDisplay selectedCardInfo={selectedCardInfo} />
        </S.MyCardContainer>
        <S.CardContainer>
          <GameCardGrid cardInfos={cardInfos} onClickCard={onClickCard} />
        </S.CardContainer>
      </Layout.Content>
    </Layout>
  );
};

export default Round;
