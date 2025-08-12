import { StompSubscription } from '@stomp/stompjs';
import { useEffect, useRef } from 'react';
import { useWebSocket } from '../contexts/WebSocketContext';

export const useWebSocketSubscription = <T>(destination: string, onData: (data: T) => void) => {
  const { subscribe, isConnected, isVisible } = useWebSocket();
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

  // 재연결시 구독 상태 복구를 위한 디버깅 로그
  useEffect(() => {
    console.log(`🔍 구독 상태 변경 - ${destination}:`, {
      isConnected,
      isVisible,
      hasSubscription: !!subscriptionRef.current,
    });
  }, [isConnected, isVisible, destination]);
};
