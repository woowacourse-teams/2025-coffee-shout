import { useState, PropsWithChildren } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import { createStompClient } from '../createStompClient';
import { WebSocketContext, WebSocketContextType } from './WebSocketContext';

type WebSocketSuccess<T> = {
  success: true;
  data: T;
  errorMessage: null;
};

type WebSocketError = {
  success: false;
  data: null;
  errorMessage: string;
};

type WebSocketMessage<T> = WebSocketSuccess<T> | WebSocketError;

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const [client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  const startSocket = (): Promise<void> => {
    return new Promise((resolve, reject) => {
      if (client && isConnected) {
        resolve();
        return;
      }

      const stompClient = createStompClient();

      stompClient.onConnect = () => {
        setIsConnected(true);
        console.log('✅WebSocket 연결');
        resolve();
      };

      stompClient.onDisconnect = () => {
        setIsConnected(false);
        console.log('❌WebSocket 연결 해제');
      };

      stompClient.onStompError = (frame) => {
        console.error('❌STOMP 오류:', frame);
        setIsConnected(false);
        reject(new Error(`STOMP 오류: ${frame.headers.message}`));
      };

      setClient(stompClient);
      stompClient.activate();
    });
  };

  const stopSocket = () => {
    if (!client || !isConnected) {
      return;
    }

    client.deactivate();
    setIsConnected(false);
  };

  const subscribe = <T,>(url: string, onData: (data: T) => void): Promise<StompSubscription> => {
    return new Promise((resolve, reject) => {
      if (!client || !isConnected) {
        reject(new Error('❌ 구독 실패: WebSocket 연결 안됨'));
        return;
      }

      try {
        const requestUrl = '/topic' + url;

        const subscription = client.subscribe(requestUrl, (message) => {
          try {
            const parsedMessage = JSON.parse(message.body) as WebSocketMessage<T>;
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

        resolve(subscription);
      } catch (error) {
        reject(error);
      }
    });
  };

  const send = <T,>(url: string, body: T | null = null): Promise<void> => {
    return new Promise((resolve, reject) => {
      if (!client || !isConnected) {
        reject(new Error('❌ 메시지 전송 실패: WebSocket 연결 안됨'));
        return;
      }

      try {
        const requestUrl = '/app' + url;

        client.publish({
          destination: requestUrl,
          body: JSON.stringify(body),
        });

        resolve();
      } catch (error) {
        reject(error);
      }
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
