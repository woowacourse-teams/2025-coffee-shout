import { PropsWithChildren, useCallback, useState } from 'react';
import { CardGameContext } from './CardGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '../Identifier/IdentifierContext';
import {
  CardGameState,
  CardGameStateData,
  CardInfo,
  PlayerRank,
  PlayerScore,
} from '@/types/miniGame';
import { RoundKey } from '@/types/round';
import { useNavigate, useParams } from 'react-router-dom';

export type CardGameScoresData = {
  scores: PlayerScore[];
};
export type CardGameRanksData = {
  ranks: PlayerRank[];
};

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();
  const [startCardGame, setStartCardGame] = useState<boolean>(false);
  const [isTransition, setIsTransition] = useState(false);
  const [currentRound, setCurrentRound] = useState<RoundKey>(1);
  const [currentCardGameState, setCurrentCardGameState] = useState<CardGameState>('READY');
  const [cardInfos, setCardInfos] = useState<CardInfo[]>([]);

  const [scores, setScores] = useState<PlayerScore[]>([]);
  const [ranks, setRanks] = useState<PlayerRank[]>([]);

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

  const handleCardGameRank = useCallback((data: CardGameRanksData) => {
    const { ranks } = data;
    console.log('ranks', ranks);
    ranks.sort((a, b) => a.rank - b.rank);
    setRanks(ranks);
  }, []);

  const handleCardGameScore = useCallback((data: CardGameScoresData) => {
    const { scores } = data;
    setScores(scores);
  }, []);

  useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);
  useWebSocketSubscription(`/room/${joinCode}/rank`, handleCardGameRank);
  useWebSocketSubscription(`/room/${joinCode}/score`, handleCardGameScore);

  return (
    <CardGameContext.Provider
      value={{
        startCardGame,
        isTransition,
        currentRound,
        currentCardGameState,
        cardInfos,
        ranks,
        scores,
      }}
    >
      {children}
    </CardGameContext.Provider>
  );
};

export default CardGameProvider;
