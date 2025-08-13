import { PropsWithChildren } from 'react';
import { useWebSocketConnection } from '../hooks/useWebSocketConnection';
import { useWebSocketMessaging } from '../hooks/useWebSocketMessaging';
import { useWebSocketReconnection } from '../hooks/useWebSocketReconnection';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  // WebSocket 연결 관리
  const { client, isConnected, startSocket, stopSocket } = useWebSocketConnection();

  // WebSocket 메시징 관리
  const { subscribe, send } = useWebSocketMessaging({ client, isConnected });

  // WebSocket 재연결 관리
  const { isVisible } = useWebSocketReconnection({
    isConnected,
    startSocket,
    stopSocket,
  });

  const contextValue: WebSocketContextType = {
    startSocket,
    stopSocket,
    subscribe,
    send,
    isConnected,
    isVisible,
    client,
  };

  return <WebSocketContext.Provider value={contextValue}>{children}</WebSocketContext.Provider>;
};
