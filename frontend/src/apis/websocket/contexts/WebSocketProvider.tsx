import { reportWebsocketError } from '@/apis/utils/reportSentryError';
import { Client, IFrame } from '@stomp/stompjs';
import { PropsWithChildren, useState } from 'react';
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

    stompClient.onStompError = (frame: IFrame) => {
      const errorDetails = {
        command: frame.command,
        message: frame.headers['message'] || '알 수 없는 STOMP 오류',
        body: frame.body,
      };

      const errorMessage = `STOMP 오류 [${errorDetails.command}]: ${errorDetails.message}`;
      console.error('❌', errorMessage, errorDetails);

      reportWebsocketError(errorMessage, {
        type: 'stomp',
        extra: { errorDetails },
      });

      setIsConnected(false);
    };

    stompClient.onWebSocketError = (event) => {
      const errorMessage = `WebSocket 연결 오류: ${event.type}`;
      console.error('❌', errorMessage);

      reportWebsocketError(errorMessage, {
        type: 'connection',
        extra: {
          eventType: event.type,
          url: stompClient.webSocket?.url,
          readyState: stompClient.webSocket?.readyState,
        },
      });

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
      const errorMessage = `구독 실패 (${url}): WebSocket 연결 안됨`;
      reportWebsocketError(errorMessage, {
        type: 'subscription',
        extra: { url, isConnected, hasClient: !!client },
      });
      throw new Error('❌ ' + errorMessage);
    }

    const requestUrl = '/topic' + url;

    return client.subscribe(requestUrl, (message) => {
      try {
        const parsedMessage = JSON.parse(message.body) as WebSocketMessage<T>;
        const isSuccess = parsedMessage.success;

        if (!isSuccess) {
          const errorMessage = `구독 메시지 오류 (${url}): ${parsedMessage.errorMessage}`;
          reportWebsocketError(errorMessage, {
            type: 'subscription',
            extra: { url, messageBody: message.body },
          });
          throw new Error(parsedMessage.errorMessage);
        }

        const data = parsedMessage.data as T;

        onData(data);
      } catch (error) {
        const errorMessage = `JSON 파싱 실패 (${url}): ${error instanceof Error ? error.message : String(error)}`;
        console.error('❌', errorMessage);
        reportWebsocketError(errorMessage, {
          type: 'parsing',
          extra: { url, messageBody: message.body, originalError: String(error) },
        });
      }
    });
  };

  const send = <T,>(url: string, body?: T) => {
    if (!client || !isConnected) {
      const errorMessage = `메시지 전송 실패 (${url}): WebSocket 연결 안됨`;
      console.warn('❌', errorMessage);
      reportWebsocketError(errorMessage, {
        type: 'send',
        extra: { url, isConnected, hasClient: !!client },
      });
      return;
    }

    const requestUrl = '/app' + url;

    try {
      let payload: string;
      if (body == null) {
        payload = '';
      } else if (typeof body === 'object') {
        payload = JSON.stringify(body);
      } else {
        payload = String(body);
      }

      client.publish({
        destination: requestUrl,
        body: payload,
      });
    } catch (error) {
      const errorMessage = `메시지 전송 중 오류 (${url}): ${error instanceof Error ? error.message : String(error)}`;
      console.error('❌', errorMessage);
      reportWebsocketError(errorMessage, {
        type: 'send',
        extra: { url, body: String(body), originalError: String(error) },
      });
    }
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
