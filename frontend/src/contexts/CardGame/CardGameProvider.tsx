import { PropsWithChildren, useCallback, useState } from 'react';
import { CardGameContext } from './CardGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameState, CardGameStateData, CardInfo } from '@/types/miniGame';
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
  const [cardInfos, setCardInfos] = useState<CardInfo[]>([]);

  const handleCardGameState = useCallback(
    (data: CardGameStateData) => {
      const { cardGameState, currentRound, cardInfoMessages } = data;

      const isFirstRoundPlaying = cardGameState === 'PLAYING' && currentRound === 'FIRST';
      const isSecondRoundLoading = cardGameState === 'LOADING' && currentRound === 'SECOND';
      const isSecondRoundPlaying = cardGameState === 'PLAYING' && currentRound === 'SECOND';
      const isSecondRoundScoreBoard = cardGameState === 'SCORE_BOARD' && currentRound === 'SECOND';
      const isGameDone = cardGameState === 'DONE';

      if (isFirstRoundPlaying) {
        setStartCardGame(true);
        setCardInfos(cardInfoMessages);
        return;
      }

      if (isSecondRoundLoading) {
        setIsTransition(true);
        setCurrentRound(2);
        setCurrentCardGameState('LOADING');
        return;
      }

      if (isSecondRoundPlaying) {
        setIsTransition(false);
        setCardInfos(cardInfoMessages);
        setCurrentCardGameState('PLAYING');
        return;
      }
      if (isSecondRoundScoreBoard) {
        setCurrentCardGameState('SCORE_BOARD');
        return;
      }
      if (isGameDone) {
        navigate(`/room/${joinCode}/${miniGameType}/result`);
        return;
      }
    },
    [navigate, joinCode, miniGameType]
  );

  useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);

  return (
    <CardGameContext.Provider
      value={{ startCardGame, isTransition, currentRound, currentCardGameState, cardInfos }}
    >
      {children}
    </CardGameContext.Provider>
  );
};

export default CardGameProvider;
