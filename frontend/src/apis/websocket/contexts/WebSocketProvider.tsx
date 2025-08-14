import { PropsWithChildren } from 'react';
import { useWebSocketConnection } from '../hooks/useWebSocketConnection';
import { useWebSocketMessaging } from '../hooks/useWebSocketMessaging';
import { useWebSocketReconnection } from '../hooks/useWebSocketReconnection';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const { client, isConnected, startSocket, stopSocket } = useWebSocketConnection();

  const { subscribe, send } = useWebSocketMessaging({ client, isConnected });

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
