import { PropsWithChildren } from 'react';
import { useWebSocketConnection } from '../hooks/useWebSocketConnection';
import { useWebSocketMessaging } from '../hooks/useWebSocketMessaging';
import { useWebSocketReconnection } from '../hooks/useWebSocketReconnection';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';
import { useStompSessionWatcher } from '../hooks/useStompSessionWatcher';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const { client, isConnected, startSocket, stopSocket } = useWebSocketConnection();
  const { sessionId } = useStompSessionWatcher(client);

  const { subscribe, send } = useWebSocketMessaging({ client, isConnected });

  useWebSocketReconnection({
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
    client,
    sessionId,
  };

  return <WebSocketContext.Provider value={contextValue}>{children}</WebSocketContext.Provider>;
};
