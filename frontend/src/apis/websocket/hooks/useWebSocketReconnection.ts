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

  const attemptReconnect = useCallback(() => {
    if (reconnectAttemptsRef.current >= WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS) {
      console.log(
        `❌ 최대 재연결 시도 횟수 초과 (${WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS}회) - 재연결 중단`
      );
      wasConnectedBeforeBackground.current = false;
      return;
    }

    console.log(
      `📱 앱이 포그라운드로 전환됨 - 웹소켓 재연결 시도 (시도: ${
        reconnectAttemptsRef.current + 1
      }/${WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS})`
    );

    clearReconnectTimeout();

    reconnectTimeoutRef.current = window.setTimeout(() => {
      console.log('🔄 웹소켓 재연결 시작');
      reconnectAttemptsRef.current += 1;
      startSocket(joinCode, myName, menuId);
    }, WEBSOCKET_CONFIG.RECONNECT_DELAY_MS);
  }, [startSocket, joinCode, myName, menuId, clearReconnectTimeout]);

  // 앱 전환 감지 및 재연결 로직
  useEffect(() => {
    if (!isVisible) {
      // 백그라운드이고, 웹소켓이 끊기지 않은 경우
      if (!isConnected) return;

      // 백그라운드이고, 웹소켓이 끊긴 경우
      wasConnectedBeforeBackground.current = true;
      console.log('📱 앱이 백그라운드로 전환됨 - 웹소켓 연결 해제');
      stopSocket();
    } else if (wasConnectedBeforeBackground.current) {
      // 현재 포그라운드이고, 이전에 한 번 웹소켓이 연결된 경우, 재연결 시도
      attemptReconnect();
    }

    return () => {
      clearReconnectTimeout();
    };
  }, [isVisible, isConnected, stopSocket, attemptReconnect, clearReconnectTimeout]);

  // 연결 성공 시 재연결 시도 횟수 리셋
  useEffect(() => {
    if (isConnected) {
      resetReconnectAttempts();
    }
  }, [isConnected, resetReconnectAttempts]);

  return {
    isVisible,
  };
};
