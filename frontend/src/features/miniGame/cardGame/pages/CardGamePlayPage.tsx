import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import PrepareOverlay from '@/features/miniGame/components/PrepareOverlay/PrepareOverlay';
import RoundTransition from '../../components/RoundTransition/RoundTransition';
import Round from '../components/Round/Round';
import { useCardGameActions } from '../hooks/useCardGameActions';
import { useCardGameTimer } from '../hooks/useCardGameTimer';

const CardGamePlayPage = () => {
  const { isTransition, currentRound, currentCardGameState, selectedCardInfo, cardInfos } =
    useCardGame();
  const { selectCard } = useCardGameActions();
  const { currentTime, isTimerActive, roundTotalTime } = useCardGameTimer();

  if (isTransition) {
    return <RoundTransition currentRound={currentRound} />;
  }

  const showPrepareOverlay = currentCardGameState === 'PREPARE';
  const isCardClickDisabled =
    currentCardGameState === 'PREPARE' || currentCardGameState === 'SCORE_BOARD';

  const onCardClick = (cardIndex: number) => {
    if (isCardClickDisabled) return;
    selectCard(cardIndex);
  };

  return (
    <>
      <Round
        round={currentRound}
        roundTotalTime={roundTotalTime}
        onClickCard={onCardClick}
        selectedCardInfo={selectedCardInfo}
        currentTime={currentTime}
        isTimerActive={isTimerActive}
        cardInfos={cardInfos}
      />
      {showPrepareOverlay && <PrepareOverlay />}
    </>
  );
};

export default CardGamePlayPage;
