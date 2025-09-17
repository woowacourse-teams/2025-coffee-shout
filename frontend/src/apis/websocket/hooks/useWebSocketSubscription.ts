import { usePageVisibility } from '@/hooks/usePageVisibility';
import { StompSubscription } from '@stomp/stompjs';
import { useCallback, useEffect, useRef } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { isVisible } = usePageVisibility();
  const { subscribe, isConnected, client } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);
  const lastConnectedRef = useRef(false);

  const unsubscribe = useCallback(() => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
      subscriptionRef.current = null;
      console.log(`🔌 웹소켓 구독 해제: ${destination}`);
    }
  }, [destination]);

  useEffect(() => {
    if (!isConnected || !isVisible) {
      unsubscribe();
      lastConnectedRef.current = false;
      return;
    }

    const shouldResubscribe =
      !lastConnectedRef.current || (lastConnectedRef.current && !subscriptionRef.current);

    if (shouldResubscribe) {
      try {
        const subscription = subscribe<T>(destination, onData);
        subscriptionRef.current = subscription;
        lastConnectedRef.current = true;
        console.log(`✅ 웹소켓 구독 성공: ${destination}`);
      } catch (error) {
        console.error('❌ 웹소켓 구독 실패:', error);
      }
    }

    return unsubscribe;
  }, [isConnected, isVisible, subscribe, destination, onData, client, unsubscribe]);
};
