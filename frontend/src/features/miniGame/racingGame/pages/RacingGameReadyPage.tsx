import { useRacingGame } from '@/contexts/RacingGame/RacingGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GameIntroSlides from '../../components/GameIntroSlides/GameIntroSlides';

const RacingGameReadyPage = () => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { racingGameState } = useRacingGame();

  useEffect(() => {
    if (!joinCode) return;
    if (racingGameState === 'PREPARE') {
      navigate(`/room/${joinCode}/RACING_GAME/play`);
    }
  }, [racingGameState, joinCode, navigate]);

  return <GameIntroSlides gameType="RACING_GAME" />;
};

export default RacingGameReadyPage;
