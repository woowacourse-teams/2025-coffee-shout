import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePageVisibility } from '@/hooks/usePageVisibility';
import { useCallback, useEffect, useRef } from 'react';

type Props = {
  isConnected: boolean;
  startSocket: (joinCode: string, myName: string) => void;
  stopSocket: () => void;
};

export const useWebSocketReconnection = ({ isConnected, startSocket, stopSocket }: Props) => {
  const { isVisible } = usePageVisibility();
  const { joinCode, myName } = useIdentifier();
  const reconnectTimerRef = useRef<number | null>(null);
  const wasBackgrounded = useRef(false);
  const hasCheckedRefresh = useRef(false);

  const clearReconnectTimer = useCallback(() => {
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
  }, []);

  const scheduleReconnect = useCallback(() => {
    clearReconnectTimer();
    reconnectTimerRef.current = window.setTimeout(() => {
      if (joinCode && myName) startSocket(joinCode, myName);
    }, 200);
  }, [joinCode, myName, startSocket, clearReconnectTimer]);

  /**
   * 새로고침 감지
   */
  useEffect(() => {
    if (hasCheckedRefresh.current) return;

    let isReload = false;

    try {
      const navigationEntries = performance.getEntriesByType(
        'navigation'
      ) as PerformanceNavigationTiming[];
      isReload = navigationEntries.length > 0 && navigationEntries[0].type === 'reload';
    } catch (error) {
      console.warn('performance.getEntriesByType not supported:', error);
      isReload = document.referrer === window.location.href;
    }

    if (isReload && !isConnected && joinCode && myName && startSocket) {
      console.log('🔄 새로고침 감지 - 웹소켓 재연결 시도:', { myName, joinCode });
      hasCheckedRefresh.current = true;
      startSocket(joinCode, myName);
    }
  }, [myName, joinCode, isConnected, startSocket]);

  /**
   * 백그라운드 ↔ 포그라운드 감지
   */
  useEffect(() => {
    if (!isVisible && isConnected) {
      console.log('📱 백그라운드 전환 - 소켓 연결 해제');
      wasBackgrounded.current = true;
      stopSocket();
    }

    if (isVisible && !isConnected && joinCode && myName && wasBackgrounded.current) {
      wasBackgrounded.current = false;
      console.log('📱 포그라운드 복귀 - 소켓 재연결');
      scheduleReconnect();
    }

    return () => clearReconnectTimer();
  }, [
    isVisible,
    isConnected,
    joinCode,
    myName,
    startSocket,
    stopSocket,
    scheduleReconnect,
    clearReconnectTimer,
  ]);

  /**
   * 온라인/오프라인 감지
   */
  useEffect(() => {
    const handleOnline = () => {
      if (!isConnected && joinCode && myName) scheduleReconnect();
    };
    const handleOffline = () => {
      if (isConnected) stopSocket();
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearReconnectTimer();
    };
  }, [
    isConnected,
    joinCode,
    myName,
    startSocket,
    stopSocket,
    scheduleReconnect,
    clearReconnectTimer,
  ]);
};
