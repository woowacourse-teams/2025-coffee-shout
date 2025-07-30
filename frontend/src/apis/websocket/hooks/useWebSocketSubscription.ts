import { useEffect, useRef } from 'react';
import { StompSubscription } from '@stomp/stompjs';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { subscribe, isConnected } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);

  useEffect(() => {
    if (!isConnected) {
      return;
    }

    try {
      const subscription = subscribe<T>(destination, onData);
      subscriptionRef.current = subscription;

      return () => {
        if (subscriptionRef.current) {
          subscriptionRef.current.unsubscribe();
          subscriptionRef.current = null;
        }
      };
    } catch (error) {
      console.error('❌ 웹소켓 구독 실패:', error);
    }
  }, [isConnected, subscribe, destination, onData]);
};
