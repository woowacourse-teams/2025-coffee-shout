import { useState, PropsWithChildren } from 'react';
import { Client } from '@stomp/stompjs';
import { createStompClient } from '../createStompClient';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const [client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const startSocket = () => {
    if (client && isConnected) {
      return;
    }

    const stompClient = createStompClient();

    stompClient.onConnect = () => {
      setIsConnected(true);
      console.log('✅WebSocket 연결');
    };

    stompClient.onDisconnect = () => {
      setIsConnected(false);
      console.log('❌WebSocket 연결 해제');
    };

    stompClient.onStompError = (frame) => {
      console.error('❌STOMP 오류:', frame);
      setIsConnected(false);
    };

    setClient(stompClient);
    stompClient.activate();
  };

  const stopSocket = () => {
    if (!client || !isConnected) {
      return;
    }

    client.deactivate();
    setIsConnected(false);
  };

  const subscribe = <T,>(destination: string, onPayload: (payload: T) => void) => {
    if (!client || !isConnected) {
      throw new Error('❌ 구독 실패: WebSocket 연결 안됨');
    }

    return client.subscribe(destination, (message) => {
      try {
        const parsedPayload = JSON.parse(message.body) as T;
        onPayload(parsedPayload);
      } catch (error) {
        console.error('❌ 페이로드 파싱 실패:', error);
      }
    });
  };

  const send = <T,>(destination: string, body: T) => {
    if (!client || !isConnected) {
      console.warn('❌ 메시지 전송 실패: WebSocket 연결 안됨');
      return;
    }

    client.publish({
      destination,
      body: JSON.stringify(body),
    });
  };

  const contextValue: WebSocketContextType = {
    startSocket,
    stopSocket,
    subscribe,
    send,
    isConnected,
    client,
  };

  return <WebSocketContext.Provider value={contextValue}>{children}</WebSocketContext.Provider>;
};
