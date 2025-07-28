import { Client } from '@stomp/stompjs';
import { createContext, useContext } from 'react';

interface WebSocketContextType {
  client: Client | null;
  connect: () => void;
  disconnect: () => void;
  isConnected: boolean;
}

export const WebSocketContext = createContext<WebSocketContextType | null>(null);

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};
