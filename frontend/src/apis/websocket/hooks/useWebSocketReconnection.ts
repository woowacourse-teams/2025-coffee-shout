import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useCallback, useEffect, useRef } from 'react';

type Props = {
  isConnected: boolean;
  isVisible: boolean;
  startSocket: (joinCode: string, myName: string) => void;
  stopSocket: () => void;
};

export const useWebSocketReconnection = ({
  isConnected,
  isVisible,
  startSocket,
  stopSocket,
}: Props) => {
  const { joinCode, myName } = useIdentifier();
  const reconnectTimerRef = useRef<number | null>(null);
  const wasBackgrounded = useRef(false);

  const scheduleReconnect = useCallback(() => {
    if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
    reconnectTimerRef.current = window.setTimeout(() => {
      if (joinCode && myName) startSocket(joinCode, myName);
    }, 200);
  }, [joinCode, myName, startSocket]);

  /**
   * 백그라운드 ↔ 포그라운드
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

    return () => {
      if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
    };
  }, [isVisible, isConnected, joinCode, myName, startSocket, stopSocket, scheduleReconnect]);

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
      if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
    };
  }, [isConnected, joinCode, myName, startSocket, stopSocket, scheduleReconnect]);
};
