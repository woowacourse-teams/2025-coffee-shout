import { createContext, useContext } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';

export type WebSocketContextType = {
  startSocket: () => Promise<void>;
  stopSocket: () => void;
  send: <T>(destination: string, body?: T) => void;
  subscribe: <T>(destination: string, onData: (data: T) => void) => Promise<StompSubscription>;
  isConnected: boolean;
  client: Client | null;
};

export const WebSocketContext = createContext<WebSocketContextType | null>(null);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};
