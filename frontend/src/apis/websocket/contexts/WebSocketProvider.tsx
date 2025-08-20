import { PropsWithChildren } from 'react';
import { useBackgroundRedirect } from '../hooks/useBackgroundRedirect';
import { usePageVisibility } from '../hooks/usePageVisibility';
import { useWebSocketConnection } from '../hooks/useWebSocketConnection';
import { useWebSocketMessaging } from '../hooks/useWebSocketMessaging';
import { useWebSocketReconnection } from '../hooks/useWebSocketReconnection';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const { isVisible } = usePageVisibility();

  const { client, isConnected, startSocket, stopSocket } = useWebSocketConnection();

  const { subscribe, send } = useWebSocketMessaging({ client, isConnected });

  useWebSocketReconnection({
    isConnected,
    isVisible,
    startSocket,
    stopSocket,
  });

  useBackgroundRedirect({
    isConnected,
    isVisible,
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
