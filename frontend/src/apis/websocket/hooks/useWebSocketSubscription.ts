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
  const retryCountRef = useRef(0);
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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

    // 재시도 타이머 정리
    if (retryTimerRef.current) {
      clearTimeout(retryTimerRef.current);
      retryTimerRef.current = null;
    }
  }, [destination]);

  const trySubscribe = useCallback(() => {
    if (!enabled || !isVisible || !isConnected) {
      console.log(
        `🚫 구독 조건 불충족 (enabled=${enabled}, visible=${isVisible}, connected=${isConnected})`
      );
      return;
    }

    try {
      const sub = subscribe<T>(destination, onData, onError);
      subscriptionRef.current = sub;
      lastSessionIdRef.current = sessionId;
      retryCountRef.current = 0; // 성공 시 재시도 카운트 초기화
      console.log(`✅ 구독 성공: ${destination}`, { sessionId });
    } catch (error) {
      console.error(`❌ 구독 실패 (시도 ${retryCountRef.current + 1})`, error);

      if (retryCountRef.current < 5) {
        const delay = Math.min(1000 * 2 ** retryCountRef.current, 10000); // 1s → 2s → 4s → 8s → 10s
        retryCountRef.current += 1;
        retryTimerRef.current = setTimeout(() => {
          console.log(`⏳ ${destination} 재시도 (${retryCountRef.current}회차)...`);
          trySubscribe();
        }, delay);
      } else {
        console.error(`🚫 ${destination} 구독 재시도 횟수 초과 (5회)`);
      }
    }
  }, [enabled, isVisible, isConnected, destination, onData, onError, sessionId, subscribe]);

  const doSubscribe = useCallback(() => {
    const sessionChanged = sessionId !== lastSessionIdRef.current;
    if (sessionChanged || !subscriptionRef.current) {
      if (sessionChanged) {
        console.log(`🔄 SessionId 변경 감지: ${destination}`);
        unsubscribe();
      }
      trySubscribe();
    }
  }, [sessionId, destination, unsubscribe, trySubscribe]);

  useEffect(() => {
    if (isConnected) doSubscribe();
    else unsubscribe();

    return unsubscribe;
  }, [isConnected, doSubscribe, unsubscribe]);
};
