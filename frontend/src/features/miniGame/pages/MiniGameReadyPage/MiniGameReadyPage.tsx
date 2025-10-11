import CardGameReadyPage from '@/features/miniGame/cardGame/pages/CardGameReadyPage';
import RacingGameReadyPage from '@/features/miniGame/racingGame/pages/RacingGameReadyPage';
import { MiniGameType } from '@/types/miniGame/common';
import { JSX } from 'react';
import { useParams } from 'react-router-dom';

const READY_PAGE_COMPONENTS: Record<MiniGameType, () => JSX.Element> = {
  CARD_GAME: CardGameReadyPage,
  RACING_GAME: RacingGameReadyPage,
} as const;

const MiniGameReadyPage = () => {
  const { miniGameType } = useParams();

  if (!miniGameType || !(miniGameType in READY_PAGE_COMPONENTS)) {
    return (
      <div>
        <h1>잘못된 미니게임입니다.</h1>
        <p>지원하지 않는 미니게임 타입: {miniGameType}</p>
      </div>
    );
  }

  const ReadyPageComponent = READY_PAGE_COMPONENTS[miniGameType as MiniGameType];

  return <ReadyPageComponent />;
};

export default MiniGameReadyPage;
