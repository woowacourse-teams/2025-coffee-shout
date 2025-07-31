import { PropsWithChildren, useCallback, useState } from 'react';
import { CardGameContext } from './CardGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameState, CardGameStateData } from '@/types/miniGame';
import { RoundKey } from '@/types/round';
import { useNavigate, useParams } from 'react-router-dom';

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();
  const [startCardGame, setStartCardGame] = useState<boolean>(false);
  const [isTransition, setIsTransition] = useState(false);
  const [currentRound, setCurrentRound] = useState<RoundKey>(1);
  const [currentCardGameState, setCurrentCardGameState] = useState<CardGameState>('READY');

  const handleCardGameState = useCallback((data: CardGameStateData) => {
    if (data.cardGameState === 'PLAYING' && data.currentRound === 'FIRST') {
      setStartCardGame(true);
    }
    if (data.cardGameState === 'LOADING' && data.currentRound === 'SECOND') {
      setIsTransition(true);
      setCurrentRound(2);
      setCurrentCardGameState('LOADING');
    }
    if (data.cardGameState === 'PLAYING' && data.currentRound === 'SECOND') {
      setIsTransition(false);
      setCurrentCardGameState('PLAYING');
    }
    if (data.cardGameState === 'DONE') {
      navigate(`/room/${joinCode}/${miniGameType}/result`);
    }
    console.log(data);
  }, []);

  useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);

  return (
    <CardGameContext.Provider
      value={{ startCardGame, isTransition, currentRound, currentCardGameState }}
    >
      {children}
    </CardGameContext.Provider>
  );
};

export default CardGameProvider;
