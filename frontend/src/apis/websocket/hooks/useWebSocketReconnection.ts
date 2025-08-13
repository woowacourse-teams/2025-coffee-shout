import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useCallback, useEffect, useRef } from 'react';
import { WEBSOCKET_CONFIG } from '../constants/constants';
import { usePageVisibility } from './usePageVisibility';

type Props = {
  isConnected: boolean;
  startSocket: (joinCode: string, myName: string, menuId: number) => void;
  stopSocket: () => void;
};

export const useWebSocketReconnection = ({ isConnected, startSocket, stopSocket }: Props) => {
  // TODO: 웹소켓 provider에 도메인 정보가 있는 것은 좋지 않음. 추후 리팩토링 필요
  const { joinCode, myName, menuId } = useIdentifier();
  const { isVisible } = usePageVisibility();
  const wasConnectedBeforeBackground = useRef(false);
  const reconnectTimeoutRef = useRef<number | null>(null);
  const reconnectAttemptsRef = useRef(0);

  const resetReconnectAttempts = useCallback(() => {
    reconnectAttemptsRef.current = 0;
    wasConnectedBeforeBackground.current = false;
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

  const logReconnectAttempt = useCallback(() => {
    console.log(
      `📱 앱이 포그라운드로 전환됨 - 웹소켓 재연결 시도 (시도: ${
        reconnectAttemptsRef.current + 1
      }/${WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS})`
    );
  }, []);

  const scheduleReconnect = useCallback(() => {
    reconnectTimeoutRef.current = window.setTimeout(() => {
      console.log('🔄 웹소켓 재연결 시작');
      reconnectAttemptsRef.current += 1;
      startSocket(joinCode, myName, menuId);
    }, WEBSOCKET_CONFIG.RECONNECT_DELAY_MS);
  }, [startSocket, joinCode, myName, menuId]);

  const attemptReconnect = useCallback(() => {
    if (checkAndHandleMaxAttempts()) return;
    logReconnectAttempt();
    clearReconnectTimeout();
    scheduleReconnect();
  }, [checkAndHandleMaxAttempts, logReconnectAttempt, clearReconnectTimeout, scheduleReconnect]);

  /**
   * 앱 전환 감지 및 재연결 로직
   */
  useEffect(() => {
    if (!isVisible) {
      if (isConnected) {
        wasConnectedBeforeBackground.current = true;
        console.log('📱 앱이 백그라운드로 전환됨 - 웹소켓 연결 해제');
        stopSocket();
      }
    } else if (wasConnectedBeforeBackground.current) {
      attemptReconnect();
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

  return {
    isVisible,
  };
};
