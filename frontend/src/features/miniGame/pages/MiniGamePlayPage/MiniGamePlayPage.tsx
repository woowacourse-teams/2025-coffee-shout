import CardGamePlayPage from '@/features/miniGame/cardGame/pages/CardGamePlayPage';
import { MiniGameType } from '@/types/miniGame/common';
import { JSX } from 'react';
import { useParams } from 'react-router-dom';

const MINI_GAME_COMPONENTS: Record<MiniGameType, () => JSX.Element> = {
  CARD_GAME: CardGamePlayPage,
  // '31_GAME': Random31GamePlayPage,
} as const;

const MiniGamePlayPage = () => {
  const { miniGameType } = useParams();

  if (!miniGameType || !(miniGameType in MINI_GAME_COMPONENTS)) {
    // TODO: 에러 화면 추후 수정 필요
    return (
      <div>
        <h1>잘못된 미니게임입니다.</h1>
        <p>지원하지 않는 미니게임 타입: {miniGameType}</p>
      </div>
    );
  }

  const GameComponent = MINI_GAME_COMPONENTS[miniGameType as MiniGameType];

  return <GameComponent />;
};

export default MiniGamePlayPage;
