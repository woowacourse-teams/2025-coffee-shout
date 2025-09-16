import { Client, IFrame } from '@stomp/stompjs';
import { useCallback, useState } from 'react';
import { createStompClient } from '../utils/createStompClient';
import WebSocketErrorHandler from '../utils/WebSocketErrorHandler';

export const useWebSocketConnection = () => {
  const [client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const handleConnect = useCallback(() => {
    setIsConnected(true);
    console.log('✅ WebSocket 연결');
  }, []);

  const handleDisconnect = useCallback(() => {
    setIsConnected(false);
    console.log('❌ WebSocket 연결 해제');
  }, []);

  const handleStompError = useCallback((frame: IFrame) => {
    WebSocketErrorHandler.handleStompError(frame);
    setIsConnected(false);
  }, []);

  const handleWebSocketError = useCallback((event: Event, stompClient: Client) => {
    WebSocketErrorHandler.handleWebSocketError(event, stompClient);
    setIsConnected(false);
  }, []);

  const setupStompClient = useCallback(
    (joinCode: string, myName: string): Client => {
      const stompClient = createStompClient({
        joinCode,
        playerName: myName,
      });

      stompClient.onConnect = handleConnect;
      stompClient.onDisconnect = handleDisconnect;
      stompClient.onStompError = handleStompError;
      stompClient.onWebSocketError = (event: Event) => handleWebSocketError(event, stompClient);

      return stompClient;
    },
    [handleConnect, handleDisconnect, handleStompError, handleWebSocketError]
  );

  const validateClient = useCallback(() => {
    if (client && isConnected) {
      console.log('⚠️ 이미 연결된 클라이언트가 있습니다. 중복 연결을 방지합니다.');
      return false;
    }

    if (client && !isConnected) {
      console.log('🧹 이전 클라이언트 정리 중...');
      client.deactivate();
      setClient(null);
    }

    return true;
  }, [client, isConnected]);

  const validateConnectionParams = useCallback((joinCode: string, myName: string) => {
    if (!joinCode || !myName) {
      console.error('❌ WebSocket 연결 실패: 참여코드 또는 이름이 없습니다.', {
        joinCode,
        myName,
      });
      return false;
    }
    return true;
  }, []);

  const startSocket = useCallback(
    (joinCode: string, myName: string) => {
      if (!validateClient() || !validateConnectionParams(joinCode, myName)) return;

      console.log('🚀 WebSocket 연결 시작...', { joinCode, myName });
      const stompClient = setupStompClient(joinCode, myName);
      setClient(stompClient);
      stompClient.activate();
    },
    [validateClient, validateConnectionParams, setupStompClient]
  );

  const stopSocket = useCallback(() => {
    if (!client) return;

    console.log('🛑 WebSocket 연결 종료...');
    client.deactivate();
    setIsConnected(false);
    setClient(null);
  }, [client]);

  return {
    client,
    isConnected,
    startSocket,
    stopSocket,
  };
};
