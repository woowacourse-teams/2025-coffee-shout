import { CardGameState, CardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';
import React, { useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useIdentifier } from '../../Identifier/IdentifierContext';
import { Action } from '../reducer/cardGameReducer';

type CardGameStateResponse = {
  cardGameState: CardGameState;
  currentRound: RoundType;
  cardInfoMessages: CardInfo[];
  allSelected: boolean;
};

type CardGameStateHandlers = {
  updateSelectedCardInfo: (
    cardInfoMessages: CardInfo[],
    round: RoundType,
    shouldCheckAlreadySelected?: boolean
  ) => void;
};

export const useCardGameHandlers = (
  dispatch: React.Dispatch<Action>,
  { updateSelectedCardInfo }: CardGameStateHandlers
) => {
  const navigate = useNavigate();
  const { joinCode } = useIdentifier();
  const { miniGameType } = useParams();

  const handleCardGameState = useCallback(
    (data: CardGameStateResponse) => {
      const { cardGameState, currentRound, cardInfoMessages } = data;

      switch (cardGameState) {
        case 'PREPARE':
          dispatch({ type: 'PREPARE', payload: { cardInfos: cardInfoMessages } });
          break;

        case 'PLAYING':
          dispatch({
            type: 'PLAYING',
            payload: { cardInfos: cardInfoMessages, round: currentRound },
          });
          updateSelectedCardInfo(cardInfoMessages, currentRound);
          break;

        case 'SCORE_BOARD':
          dispatch({
            type: 'SCORE_BOARD',
            payload: { cardInfos: cardInfoMessages, round: currentRound },
          });
          updateSelectedCardInfo(cardInfoMessages, currentRound, true);
          break;

        case 'LOADING':
          dispatch({ type: 'LOADING', payload: { currentRound } });
          break;

        case 'DONE':
          dispatch({ type: 'DONE' });
          navigate(`/room/${joinCode}/${miniGameType}/result`);
          break;
      }
    },
    [dispatch, updateSelectedCardInfo, navigate, joinCode, miniGameType]
  );

  return {
    handleCardGameState,
  };
};
