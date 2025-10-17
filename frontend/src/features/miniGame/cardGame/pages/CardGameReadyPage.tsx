import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GameIntroSlides from '../../components/GameIntroSlides/GameIntroSlides';

const CardGameReadyPage = () => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { currentCardGameState } = useCardGame();

  useEffect(() => {
    if (!joinCode) return;
    if (currentCardGameState === 'PREPARE') {
      navigate(`/room/${joinCode}/CARD_GAME/play`);
    }
  }, [currentCardGameState, joinCode, navigate]);

  return <GameIntroSlides gameType="CARD_GAME" />;
};

export default CardGameReadyPage;
