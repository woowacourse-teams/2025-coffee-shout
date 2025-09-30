import { MiniGameType } from '@/types/miniGame/common';
import { JSX } from 'react';
import { useParams } from 'react-router-dom';
import CardGameReadyPage from '../../cardGame/pages/CardGameReadyPage/CardGameReadyPage';

const MINI_GAME_READY_COMPONENTS: Record<MiniGameType, () => JSX.Element> = {
  CARD_GAME: CardGameReadyPage,
} as const;

const MiniGameReadyPage = () => {
  const { miniGameType } = useParams();

  if (!miniGameType || !(miniGameType in MINI_GAME_READY_COMPONENTS)) {
    return (
      <div>
        <h1>잘못된 미니게임입니다.</h1>
        <p>지원하지 않는 미니게임 타입: {miniGameType}</p>
      </div>
    );
  }

  const ReadyComponent = MINI_GAME_READY_COMPONENTS[miniGameType as MiniGameType];

  return <ReadyComponent />;
};

export default MiniGameReadyPage;
