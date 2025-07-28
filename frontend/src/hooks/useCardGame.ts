import { useEffect, useState } from 'react';
import { useWebSocket } from '@/contexts/WebSocket/WebSocketContext';
import type { CardGameStateResponse, CardGameState } from '@/types/cardGame';

interface UseCardGameReturn {
  cardGameState: CardGameState | null;
  currentRound: number | null;
  cardInfoMessages: CardGameStateResponse['cardInfoMessages'] | null;
  allSelected: boolean | null;
  isLoading: boolean;
  error: string | null;
}

const useCardGame = (joinCode: string): UseCardGameReturn => {
  const { client, isConnected } = useWebSocket();
  const [cardGameState, setCardGameState] = useState<CardGameState | null>(null);
  const [currentRound, setCurrentRound] = useState<number | null>(null);
  const [cardInfoMessages, setCardInfoMessages] = useState<
    CardGameStateResponse['cardInfoMessages'] | null
  >(null);
  const [allSelected, setAllSelected] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isConnected || !client || !joinCode) {
      return;
    }

    const subscription = client.subscribe(`/topic/room/${joinCode}/cardgame/state`, (message) => {
      try {
        const data: CardGameStateResponse = JSON.parse(message.body);

        setCardGameState(data.cardGameState);
        setCurrentRound(data.currentRound);
        setCardInfoMessages(data.cardInfoMessages);
        setAllSelected(data.allSelected);
        setError(null);
        setIsLoading(false);
      } catch (err) {
        console.error('카드 게임 상태 파싱 오류:', err);
        setError('카드 게임 상태를 불러오는 중 오류가 발생했습니다.');
        setIsLoading(false);
      }
    });

    return () => {
      subscription.unsubscribe();
    };
  }, [client, isConnected, joinCode]);

  return {
    cardGameState,
    currentRound,
    cardInfoMessages,
    allSelected,
    isLoading,
    error,
  };
};

export default useCardGame;
