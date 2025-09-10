import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useCallback, useEffect, useRef } from 'react';
import { WEBSOCKET_CONFIG } from '../constants/constants';

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
  // TODO: 웹소켓 provider에 도메인 정보가 있는 것은 좋지 않음. 추후 리팩토링 필요
  const { joinCode, myName } = useIdentifier();
  const wasConnectedBeforeBackground = useRef(false);
  const wasConnectedBeforeOffline = useRef(false);
  const reconnectTimeoutRef = useRef<number | null>(null);
  const reconnectAttemptsRef = useRef(0);

  const resetReconnectAttempts = useCallback(() => {
    reconnectAttemptsRef.current = 0;
    wasConnectedBeforeBackground.current = false;
    wasConnectedBeforeOffline.current = false;
  }, []);

  const clearReconnectTimeout = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
  }, []);

  const checkAndHandleMaxAttempts = useCallback(() => {
    if (reconnectAttemptsRef.current >= WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
      console.log(
        `❌ 최대 재연결 시도 횟수 초과 (${WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS}회) - 재연결 중단`
      );
      wasConnectedBeforeBackground.current = false;
      return true;
    }
    return false;
  }, []);

  const logReconnectAttempt = useCallback((reason: string) => {
    console.log(
      `🔄 ${reason} - 웹소켓 재연결 시도 (시도: ${
        reconnectAttemptsRef.current + 1
      }/${WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS})`
    );
  }, []);

  const scheduleReconnect = useCallback(() => {
    reconnectTimeoutRef.current = window.setTimeout(() => {
      console.log('🔄 웹소켓 재연결 시작');
      reconnectAttemptsRef.current += 1;
      startSocket(joinCode, myName);
    }, WEBSOCKET_CONFIG.RECONNECT_DELAY_MS);
  }, [startSocket, joinCode, myName]);

  const attemptReconnect = useCallback(
    (reason: string) => {
      if (checkAndHandleMaxAttempts()) return;
      logReconnectAttempt(reason);
      clearReconnectTimeout();
      scheduleReconnect();
    },
    [checkAndHandleMaxAttempts, logReconnectAttempt, clearReconnectTimeout, scheduleReconnect]
  );

  /**
   * 앱 전환 감지 및 재연결 로직
   */
  useEffect(() => {
    if (!isVisible && isConnected) {
      wasConnectedBeforeBackground.current = true;
      console.log('📱 앱이 백그라운드로 전환됨 - 웹소켓 연결 해제');
      stopSocket();
    } else if (isVisible && wasConnectedBeforeBackground.current && !isConnected) {
      attemptReconnect('앱이 포그라운드로 전환됨');
    }

    return () => {
      clearReconnectTimeout();
    };
  }, [isVisible, isConnected, stopSocket, attemptReconnect, clearReconnectTimeout]);

  /**
   * 연결 성공 시 재연결 시도 횟수 리셋
   */
  useEffect(() => {
    if (isConnected) {
      resetReconnectAttempts();
    }
  }, [isConnected, resetReconnectAttempts]);

  /**
   * 네트워크 상태 변화 감지 및 재연결 로직
   */
  useEffect(() => {
    const handleOnline = () => {
      console.log('🌐 네트워크 연결됨');

      if (wasConnectedBeforeOffline.current && !isConnected) {
        attemptReconnect('네트워크 연결 복구됨');
      }
    };

    const handleOffline = () => {
      console.log('🌐 네트워크 연결 끊김');

      if (isConnected) {
        wasConnectedBeforeOffline.current = true;
        stopSocket();
      }
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, [isConnected, stopSocket, attemptReconnect]);
};
