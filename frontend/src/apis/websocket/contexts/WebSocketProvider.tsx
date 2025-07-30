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

  const subscribe = <T,>(url: string, onData: (data: T) => void) => {
    if (!client || !isConnected) {
      throw new Error('❌ 구독 실패: WebSocket 연결 안됨');
    }

    const requestUrl = '/topic' + url;

    return client.subscribe(requestUrl, (message) => {
      try {
        const parsedMessage = JSON.parse(message.body);
        const isSuccess = parsedMessage.success;

        if (!isSuccess) {
          throw new Error(parsedMessage.errorMessage);
        }

        const data = parsedMessage.data as T;

        onData(data);
      } catch (error) {
        console.error('❌ 데이터 파싱 실패:', error);
      }
    });
  };

  const send = <T,>(url: string, body: T | null = null) => {
    if (!client || !isConnected) {
      console.warn('❌ 메시지 전송 실패: WebSocket 연결 안됨');
      return;
    }

    const requestUrl = '/app' + url;

    client.publish({
      destination: requestUrl,
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
