import { createStompClient } from '@/apis/websocket/createStompClient';
import { PropsWithChildren, useEffect, useState } from 'react';
import { WebSocketContext } from './WebSocketContext';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const [client, setClient] = useState<ReturnType<typeof createStompClient> | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const connect = () => {
    if (client) {
      client.deactivate();
    }

    const stompClient = createStompClient();

    stompClient.onConnect = () => {
      console.log('✅ WebSocket 연결 성공');
      setIsConnected(true);
    };

    stompClient.onDisconnect = () => {
      console.log('🔌 WebSocket 연결 해제');
      setIsConnected(false);
    };

    setClient(stompClient);
    stompClient.activate();
  };

  const disconnect = () => {
    if (client) {
      client.deactivate();
      setClient(null);
      setIsConnected(false);
    }
  };

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, []);

  return (
    <WebSocketContext.Provider
      value={{
        client,
        connect,
        disconnect,
        isConnected,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  );
};
