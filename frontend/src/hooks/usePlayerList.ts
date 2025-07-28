import { useEffect, useState } from 'react';
import { useWebSocket } from '@/contexts/WebSocket/WebSocketContext';

interface MenuResponse {
  id: number;
  name: string;
  image: string;
}

interface Player {
  playerName: string;
  menuResponse: MenuResponse;
}

interface PlayerListResponse {
  success: boolean;
  data: Player[];
}

const usePlayerList = (joinCode: string) => {
  const { client, isConnected } = useWebSocket(); // 컨텍스트에서 직접 가져옴
  const [players, setPlayers] = useState<Player[]>([]);

  useEffect(() => {
    if (!client || !isConnected) return;

    const subscription = client.subscribe(`/topic/room/${joinCode}`, (message) => {
      try {
        const data: PlayerListResponse = JSON.parse(message.body);
        if (data.success) {
          setPlayers(data.data);
        }
      } catch (error) {
        console.error('참가자 리스트 파싱 오류:', error);
      }
    });

    console.log(`📡 참가자 리스트 구독 시작: /topic/room/${joinCode}`);

    return () => subscription.unsubscribe();
  }, [client, isConnected, joinCode]);

  return players;
};

export default usePlayerList;
