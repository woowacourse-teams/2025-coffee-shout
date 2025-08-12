import { PropsWithChildren, useCallback, useState } from 'react';
import { CardGameContext } from './CardGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameState, CardGameStateData, CardInfo } from '@/types/miniGame';

import { useNavigate, useParams } from 'react-router-dom';
import { CardGameRound } from '@/constants/miniGame';

export type SelectedCardInfo = Record<
  CardGameRound,
  {
    isSelected: boolean;
    type: string | null;
    value: number | null;
  }
>;

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const navigate = useNavigate();
  const { joinCode, myName } = useIdentifier();
  const { miniGameType } = useParams();
  const [startCardGame, setStartCardGame] = useState<boolean>(false);
  const [isTransition, setIsTransition] = useState(false);
  const [currentRound, setCurrentRound] = useState<CardGameRound>('FIRST');
  const [currentCardGameState, setCurrentCardGameState] = useState<CardGameState>('READY');
  const [cardInfos, setCardInfos] = useState<CardInfo[]>([]);
  const [selectedCardInfo, setSelectedCardInfo] = useState<SelectedCardInfo>({
    FIRST: {
      isSelected: false,
      type: null,
      value: null,
    },
    SECOND: {
      isSelected: false,
      type: null,
      value: null,
    },
  });

  const handleCardGameState = useCallback(
    (data: CardGameStateData) => {
      const { cardGameState, currentRound, cardInfoMessages } = data;

      const isFirstRoundPlaying = cardGameState === 'PLAYING' && currentRound === 'FIRST';
      const isFirstRoundScoreBoard = cardGameState === 'SCORE_BOARD' && currentRound === 'FIRST';
      const isSecondRoundLoading = cardGameState === 'LOADING' && currentRound === 'SECOND';
      const isSecondRoundPlaying = cardGameState === 'PLAYING' && currentRound === 'SECOND';
      const isSecondRoundScoreBoard = cardGameState === 'SCORE_BOARD' && currentRound === 'SECOND';
      const isGameDone = cardGameState === 'DONE';

      const handleScoreBoard = () => {
        setCurrentCardGameState('SCORE_BOARD');
        setCardInfos(cardInfoMessages);

        const mySelectedCardInfo = cardInfoMessages.find((card) => card.playerName === myName);
        if (!mySelectedCardInfo) return;
        if (selectedCardInfo[currentRound].isSelected) return;

        setSelectedCardInfo((prev) => ({
          ...prev,
          [currentRound]: {
            isSelected: true,
            type: mySelectedCardInfo.cardType,
            value: mySelectedCardInfo.value,
          },
        }));
      };

      if (isFirstRoundPlaying) {
        setStartCardGame(true);
        setCurrentCardGameState('PLAYING');
        setCardInfos(cardInfoMessages);
        return;
      }

      if (isFirstRoundScoreBoard) {
        handleScoreBoard();
        return;
      }

      if (isSecondRoundLoading) {
        setIsTransition(true);
        setCurrentRound('SECOND');
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
        handleScoreBoard();
        return;
      }

      if (isGameDone) {
        navigate(`/room/${joinCode}/${miniGameType}/result`);
        return;
      }
    },
    [navigate, joinCode, miniGameType, myName, setSelectedCardInfo, selectedCardInfo]
  );

  useWebSocketSubscription(`/room/${joinCode}/gameState`, handleCardGameState);

  return (
    <CardGameContext.Provider
      value={{
        startCardGame,
        isTransition,
        currentRound,
        currentCardGameState,
        cardInfos,
        selectedCardInfo,
        setSelectedCardInfo,
      }}
    >
      {children}
    </CardGameContext.Provider>
  );
};

export default CardGameProvider;
