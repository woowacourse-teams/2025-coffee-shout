import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { MiniGameType } from '@/types/miniGame/common';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import GameIntroSlide from '../../components/GameIntroSlide/GameIntroSlide';
import { GAME_SLIDE_CONFIGS, getGameSlideConfig } from '../../config/gameSlideConfigs';

const MiniGameReadyPage = () => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();
  const { currentCardGameState } = useCardGame();

  const isValidGameType = miniGameType && miniGameType in GAME_SLIDE_CONFIGS;
  const gameType = miniGameType as MiniGameType;
  // TODO: slideConfig가 존재하지 않으면 MiniGameReadyPage를 건너뛰게 하기
  const slideConfig = isValidGameType ? getGameSlideConfig(gameType) : [];

  useEffect(() => {
    if (!joinCode || !gameType) return;
    if (currentCardGameState === 'PREPARE') {
      navigate(`/room/${joinCode}/${gameType}/play`);
    }
  }, [currentCardGameState, joinCode, gameType, navigate]);

  if (!isValidGameType) {
    return (
      <div>
        <h1>잘못된 미니게임입니다.</h1>
        <p>지원하지 않는 미니게임 타입: {miniGameType}</p>
      </div>
    );
  }

  return (
    <Layout color="point-400">
      <Layout.Content>
        {slideConfig.map((slide, index) => (
          <GameIntroSlide
            key={index}
            textLines={slide.textLines}
            imageSrc={slide.imageSrc}
            className={slide.className}
          />
        ))}
      </Layout.Content>
    </Layout>
  );
};

export default MiniGameReadyPage;
