import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { CardGameRound } from '@/constants/miniGame';
import { CardGameState, CardGameStateData, CardInfo, SelectedCardInfo } from '@/types/miniGame';
import { PropsWithChildren, useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameContext } from './CardGameContext';

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const navigate = useNavigate();
  const { joinCode, myName } = useIdentifier();
  const { miniGameType } = useParams();
  const [isTransition, setIsTransition] = useState<boolean>(false);
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

      const isPreparing = cardGameState === 'DESCRIPTION';
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

      if (isPreparing) {
        setCurrentCardGameState('DESCRIPTION');
        setCardInfos(cardInfoMessages);
        return;
      }

      if (isFirstRoundPlaying) {
        setCurrentCardGameState('PLAYING');
        setCardInfos(cardInfoMessages);

        const myCardInfo = cardInfoMessages.find((card) => card.playerName === myName);
        if (!myCardInfo) return;

        setSelectedCardInfo((prev) => ({
          ...prev,
          [currentRound]: {
            isSelected: true,
            type: myCardInfo.cardType,
            value: myCardInfo.value,
          },
        }));
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
        const myCardInfo = cardInfoMessages.find((card) => card.playerName === myName);
        if (!myCardInfo) return;

        setSelectedCardInfo((prev) => ({
          ...prev,
          [currentRound]: {
            isSelected: true,
            type: myCardInfo.cardType,
            value: myCardInfo.value,
          },
        }));
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
