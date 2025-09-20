import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { CardGameState, CardInfo, SelectedCardInfo } from '@/types/miniGame/cardGame';
import { RoundType } from '@/types/miniGame/round';
import { PropsWithChildren, useCallback, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useIdentifier } from '../Identifier/IdentifierContext';
import { CardGameContext } from './CardGameContext';

type CardGameStateResponse = {
  cardGameState: CardGameState;
  currentRound: RoundType;
  cardInfoMessages: CardInfo[];
  allSelected: boolean;
};

const CardGameProvider = ({ children }: PropsWithChildren) => {
  const navigate = useNavigate();
  const { joinCode, myName } = useIdentifier();
  const { miniGameType } = useParams();
  const [isTransition, setIsTransition] = useState<boolean>(false);
  const [currentRound, setCurrentRound] = useState<RoundType>('FIRST');
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

  const handlePrepare = useCallback((cardInfoMessages: CardInfo[]) => {
    setCurrentCardGameState('PREPARE');
    setCardInfos(cardInfoMessages);
  }, []);

  const handlePlaying = useCallback(
    (cardInfoMessages: CardInfo[], round: RoundType) => {
      setCurrentCardGameState('PLAYING');
      setCardInfos(cardInfoMessages);

      if (round === 'SECOND') {
        setIsTransition(false);
      }

      const myCardInfo = cardInfoMessages.find((card) => card.playerName === myName);
      if (!myCardInfo) return;

      setSelectedCardInfo((prev) => ({
        ...prev,
        [round]: {
          isSelected: true,
          type: myCardInfo.cardType,
          value: myCardInfo.value,
        },
      }));
    },
    [myName]
  );

  const handleScoreBoard = useCallback(
    (cardInfoMessages: CardInfo[], round: RoundType) => {
      setCurrentCardGameState('SCORE_BOARD');
      setCardInfos(cardInfoMessages);

      const mySelectedCardInfo = cardInfoMessages.find((card) => card.playerName === myName);
      if (!mySelectedCardInfo) return;
      if (selectedCardInfo[round].isSelected) return;

      setSelectedCardInfo((prev) => ({
        ...prev,
        [round]: {
          isSelected: true,
          type: mySelectedCardInfo.cardType,
          value: mySelectedCardInfo.value,
        },
      }));
    },
    [myName, selectedCardInfo]
  );

  const handleLoading = useCallback(() => {
    setIsTransition(true);
    setCurrentRound('SECOND');
    setCurrentCardGameState('LOADING');
  }, []);

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
