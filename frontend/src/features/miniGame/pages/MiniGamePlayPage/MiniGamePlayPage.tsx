import CardGamePlayPage from '@/features/miniGame/cardGame/pages/CardGamePlayPage';
import { MiniGameType } from '@/types/miniGame/common';
import { JSX } from 'react';
import { useParams } from 'react-router-dom';

const MINI_GAME_COMPONENTS: Record<MiniGameType, () => JSX.Element> = {
  CARD_GAME: CardGamePlayPage,
} as const;

const MiniGamePlayPage = () => {
  const { miniGameType } = useParams();

  if (!miniGameType || !(miniGameType in MINI_GAME_COMPONENTS)) {
    /**
     * TODO: NotFoundPage 스타일과 동일하게 가져갈 것
     * TODO: 홈으로 돌아가기 버튼 추가
     */
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
