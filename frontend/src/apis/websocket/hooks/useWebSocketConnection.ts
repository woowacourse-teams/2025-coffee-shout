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
    setClient(null);
  }, []);

  const handleWebSocketError = useCallback((event: Event, stompClient: Client) => {
    WebSocketErrorHandler.handleWebSocketError(event, stompClient);
    setIsConnected(false);
    setClient(null);
  }, []);

  const setupStompClient = useCallback(
    (joinCode: string, myName: string): Client => {
      const stompClient = createStompClient({
        joinCode,
        playerName: myName,
      });

      stompClient.onConnect = handleConnect;
      stompClient.onDisconnect = handleDisconnect;
      stompClient.onStompError = (frame: IFrame) => handleStompError(frame);
      stompClient.onWebSocketError = (event: Event) => handleWebSocketError(event, stompClient);

      return stompClient;
    },
    [handleConnect, handleDisconnect, handleStompError, handleWebSocketError]
  );

  const validateClient = useCallback(() => {
    if (client && isConnected) return false;

    if (client && !isConnected) {
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

      const stompClient = setupStompClient(joinCode, myName);
      setClient(stompClient);
      stompClient.activate();
    },
    [validateClient, validateConnectionParams, setupStompClient]
  );

  const stopSocket = useCallback(() => {
    if (!client || !isConnected) return;

    client.deactivate();
    setIsConnected(false);
    setClient(null);
  }, [client, isConnected]);

  return {
    client,
    isConnected,
    startSocket,
    stopSocket,
  };
};
