import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { usePageVisibility } from '@/hooks/usePageVisibility';
import { StompSubscription } from '@stomp/stompjs';
import { useCallback, useEffect, useRef } from 'react';

export const useWebSocketSubscription = <T>(
  destination: string,
  onData: (data: T) => void,
  onError?: (error: Error) => void,
  enabled: boolean = true
) => {
  const { isVisible } = usePageVisibility();
  const { subscribe, isConnected, sessionId } = useWebSocket();
  const subscriptionRef = useRef<StompSubscription | null>(null);
  const lastSessionIdRef = useRef<string | null>(null);

  const unsubscribe = useCallback(() => {
    if (subscriptionRef.current) {
      try {
        subscriptionRef.current.unsubscribe();
        console.log(`🔌 구독 해제 완료: ${destination}`);
      } catch (error) {
        console.error(`❌ 구독 해제 실패: ${destination}`, error);
      } finally {
        subscriptionRef.current = null;
      }
    }
  }, [destination]);

  const doSubscribe = useCallback(() => {
    if (!enabled || !isVisible) {
      unsubscribe();
      return;
    }

    const sessionChanged = sessionId !== lastSessionIdRef.current;
    if (sessionChanged || !subscriptionRef.current) {
      if (sessionChanged) {
        console.log(`🔄 SessionId 변경 → 재구독: ${destination}`);
        unsubscribe();
      }

      try {
        const sub = subscribe<T>(destination, onData, onError);
        subscriptionRef.current = sub;
        lastSessionIdRef.current = sessionId;
        console.log(`✅ 구독 성공: ${destination}`, { sessionId });
      } catch (error) {
        console.error('❌ 구독 실패:', error);
      }
    }
  }, [enabled, isVisible, sessionId, destination, onData, onError, unsubscribe, subscribe]);

  useEffect(() => {
    if (isConnected) doSubscribe();
    return unsubscribe;
  }, [isConnected, doSubscribe, unsubscribe]);
};
