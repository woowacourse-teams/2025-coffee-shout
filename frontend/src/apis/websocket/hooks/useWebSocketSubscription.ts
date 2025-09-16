import { usePageVisibility } from '@/hooks/usePageVisibility';
import { StompSubscription } from '@stomp/stompjs';
import { useEffect, useRef } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { isVisible } = usePageVisibility();
  const { subscribe, isConnected } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);

  useEffect(() => {
    if (!isConnected || !isVisible) return;

    try {
      const subscription = subscribe<T>(destination, onData);
      subscriptionRef.current = subscription;
      console.log(`✅ 웹소켓 구독 성공: ${destination}`);

      return () => {
        if (subscriptionRef.current) {
          subscriptionRef.current.unsubscribe();
          subscriptionRef.current = null;
          console.log(`🔌 웹소켓 구독 해제: ${destination}`);
        }
      };
    } catch (error) {
      console.error('❌ 웹소켓 구독 실패:', error);
    }
  }, [isConnected, isVisible, subscribe, destination, onData]);
};
