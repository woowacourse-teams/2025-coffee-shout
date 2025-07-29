import { createContext, useContext } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';

export type WebSocketContextType = {
  startSocket: () => void;
  stopSocket: () => void;
  send: <T>(destination: string, body?: T) => void;
  subscribe: <T>(destination: string, onPayload: (payload: T) => void) => StompSubscription;
  isConnected: boolean;
  client: Client | null;
};

export const WebSocketContext = createContext<WebSocketContextType | undefined>(undefined);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};
