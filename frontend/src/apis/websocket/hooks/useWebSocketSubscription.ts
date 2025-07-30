import { useEffect, useRef, useState } from 'react';
import { StompSubscription } from '@stomp/stompjs';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { subscribe, isConnected } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);
  const [isSubscribed, setIsSubscribed] = useState(false);

  useEffect(() => {
    if (!isConnected) {
      setIsSubscribed(false);
      return;
    }

    const setupSubscription = async () => {
      try {
        const subscription = await subscribe<T>(destination, onData);
        subscriptionRef.current = subscription;
        setIsSubscribed(true);
      } catch (error) {
        console.error('❌ 웹소켓 구독 실패:', error);
        setIsSubscribed(false);
      }
    };

    setupSubscription();

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
        setIsSubscribed(false);
      }
    };
  }, [isConnected, subscribe, destination, onData]);

  return { isSubscribed };
};
