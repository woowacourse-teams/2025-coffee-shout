import { Client, IFrame } from '@stomp/stompjs';
import { useCallback, useState } from 'react';
import { ConnectionParams } from '../constants/constants';
import { createStompClient } from '../utils/createStompClient';
import WebSocketErrorHandler from '../utils/WebSocketErrorHandler';

export const useWebSocketConnection = () => {
  const [client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const validateConnectionParams = useCallback((params: ConnectionParams): boolean => {
    const { joinCode, myName, menuId } = params;
    if (!joinCode || !myName || !menuId) {
      return false;
    }
    return true;
  }, []);

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
    (params: ConnectionParams): Client => {
      const stompClient = createStompClient({
        joinCode: params.joinCode,
        playerName: params.myName,
        menuId: params.menuId,
      });

      stompClient.onConnect = handleConnect;
      stompClient.onDisconnect = handleDisconnect;
      stompClient.onStompError = (frame: IFrame) => handleStompError(frame);
      stompClient.onWebSocketError = (event: Event) => handleWebSocketError(event, stompClient);

      return stompClient;
    },
    [handleConnect, handleDisconnect, handleStompError, handleWebSocketError]
  );

  const startSocket = useCallback(
    (joinCode: string, myName: string, menuId: number) => {
      if (client && isConnected) return;

      const params = { joinCode, myName, menuId };
      if (!validateConnectionParams(params)) return;

      const stompClient = setupStompClient(params);
      setClient(stompClient);
      stompClient.activate();
    },
    [client, isConnected, validateConnectionParams, setupStompClient]
  );

  const stopSocket = useCallback(() => {
    if (!client || !isConnected) return;

    client.deactivate();
    setIsConnected(false);
  }, [client, isConnected]);

  return {
    client,
    isConnected,
    startSocket,
    stopSocket,
  };
};
