import { StompSubscription } from '@stomp/stompjs';
import { useCallback, useEffect, useRef } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { subscribe, isConnected } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);

  const memoizedOnData = useCallback(
    (data: T) => {
      onData(data);
    },
    [onData]
  );

  useEffect(() => {
    if (!isConnected) return;

    try {
      const subscription = subscribe<T>(destination, memoizedOnData);
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
  }, [isConnected, subscribe, destination, memoizedOnData]);
};
