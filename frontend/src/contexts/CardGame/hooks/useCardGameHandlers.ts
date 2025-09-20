import { CardGameState, CardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';
import { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useIdentifier } from '../../Identifier/IdentifierContext';

type CardGameStateResponse = {
  cardGameState: CardGameState;
  currentRound: RoundType;
  cardInfoMessages: CardInfo[];
  allSelected: boolean;
};

type CardGameStateHandlers = {
  updateCardGameState: (state: CardGameState) => void;
  updateCardInfos: (cardInfoMessages: CardInfo[]) => void;
  updateCurrentRound: (round: RoundType) => void;
  updateTransition: (transition: boolean) => void;
  updateSelectedCardInfo: (
    cardInfoMessages: CardInfo[],
    round: RoundType,
    shouldCheckAlreadySelected?: boolean
  ) => void;
};

export const useCardGameHandlers = ({
  updateCardGameState,
  updateCardInfos,
  updateCurrentRound,
  updateTransition,
  updateSelectedCardInfo,
}: CardGameStateHandlers) => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();

  const handlePrepare = useCallback(
    (cardInfoMessages: CardInfo[]) => {
      updateCardGameState('PREPARE');
      updateCardInfos(cardInfoMessages);
    },
    [updateCardGameState, updateCardInfos]
  );

  const handlePlaying = useCallback(
    (cardInfoMessages: CardInfo[], round: RoundType) => {
      updateCardGameState('PLAYING');
      updateCardInfos(cardInfoMessages);

      if (round === 'SECOND') {
        updateTransition(false);
      }

      updateSelectedCardInfo(cardInfoMessages, round);
    },
    [updateCardGameState, updateCardInfos, updateTransition, updateSelectedCardInfo]
  );

  const handleScoreBoard = useCallback(
    (cardInfoMessages: CardInfo[], round: RoundType) => {
      updateCardGameState('SCORE_BOARD');
      updateCardInfos(cardInfoMessages);

      updateSelectedCardInfo(cardInfoMessages, round, true);
    },
    [updateCardGameState, updateCardInfos, updateSelectedCardInfo]
  );

  const handleLoading = useCallback(() => {
    updateTransition(true);
    updateCurrentRound('SECOND');
    updateCardGameState('LOADING');
  }, [updateTransition, updateCurrentRound, updateCardGameState]);

  const handleGameDone = useCallback(() => {
    navigate(`/room/${joinCode}/${miniGameType}/result`);
  }, [navigate, joinCode, miniGameType]);

  const handleCardGameState = useCallback(
    (data: CardGameStateResponse) => {
      const { cardGameState, currentRound, cardInfoMessages } = data;

      switch (cardGameState) {
        case 'PREPARE':
          handlePrepare(cardInfoMessages);
          break;
        case 'PLAYING':
          handlePlaying(cardInfoMessages, currentRound);
          break;
        case 'SCORE_BOARD':
          handleScoreBoard(cardInfoMessages, currentRound);
          break;
        case 'LOADING':
          if (currentRound === 'SECOND') {
            handleLoading();
          }
          break;
        case 'DONE':
          handleGameDone();
          break;
      }
    },
    [handlePrepare, handlePlaying, handleScoreBoard, handleLoading, handleGameDone]
  );

  return {
    handleCardGameState,
  };
};
