import { reportWebsocketError } from '@/apis/utils/reportSentryError';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { Client, IFrame } from '@stomp/stompjs';
import { PropsWithChildren, useCallback, useEffect, useRef, useState } from 'react';
import { createStompClient } from '../createStompClient';
import { usePageVisibility } from '../hooks/usePageVisibility';
import { useReconnectionPolicy } from '../hooks/useReconnectionPolicy';
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
  const isVisible = usePageVisibility();
  const wasConnectedBeforeBackground = useRef(true);
  const reconnectTimeoutRef = useRef<number | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const maxReconnectAttempts = 3;
  const { shouldReconnect, getReconnectionDelay, currentPolicy } = useReconnectionPolicy();
  const { joinCode, myName, menuId } = useIdentifier();

  const startSocket = useCallback(
    (joinCode: string, myName: string, menuId: number) => {
      if (client && isConnected) {
        return;
      }

      // joinCode와 myName이 유효한 값인지 확인
      if (!joinCode || !myName || !menuId) {
        console.log('⚠️ WebSocket 연결 시도 건너뜀: joinCode, myName, menuId 가 없음');
        return;
      }

      const stompClient = createStompClient(joinCode, myName, menuId);

      stompClient.onConnect = () => {
        setIsConnected(true);
        reconnectAttemptsRef.current = 0; // 연결 성공시 재연결 시도 횟수 리셋
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
    },
    [client, isConnected]
  );

  const stopSocket = useCallback(() => {
    if (!client || !isConnected) {
      return;
    }

    client.deactivate();
    setIsConnected(false);
  }, [client, isConnected]);

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

  // 앱 전환 감지 및 재연결 로직
  useEffect(() => {
    if (!isVisible) {
      // 앱이 백그라운드로 전환됨
      if (isConnected) {
        wasConnectedBeforeBackground.current = true;
        console.log(`📱 앱이 백그라운드로 전환됨 - 웹소켓 연결 해제 (정책: ${currentPolicy})`);
        stopSocket();
      }
    } else {
      // 앱이 포그라운드로 전환됨
      console.log(`🔍 재연결 조건 확인:`, {
        wasConnectedBeforeBackground: wasConnectedBeforeBackground.current,
        shouldReconnect: shouldReconnect(wasConnectedBeforeBackground.current),
        currentPolicy,
        reconnectAttempts: reconnectAttemptsRef.current,
        maxReconnectAttempts,
      });

      if (shouldReconnect(wasConnectedBeforeBackground.current)) {
        // 최대 재연결 시도 횟수 체크
        if (reconnectAttemptsRef.current >= maxReconnectAttempts) {
          console.log(`❌ 최대 재연결 시도 횟수 초과 (${maxReconnectAttempts}회) - 재연결 중단`);
          wasConnectedBeforeBackground.current = false;
          return;
        }

        console.log(
          `📱 앱이 포그라운드로 전환됨 - 웹소켓 재연결 시도 (정책: ${currentPolicy}, 시도: ${reconnectAttemptsRef.current + 1}/${maxReconnectAttempts})`
        );

        // 기존 재연결 타이머가 있다면 제거
        if (reconnectTimeoutRef.current) {
          clearTimeout(reconnectTimeoutRef.current);
        }

        const delay = getReconnectionDelay();
        if (delay > 0) {
          // 지연 후 재연결 시도
          reconnectTimeoutRef.current = window.setTimeout(() => {
            // TODO: 화면에 Toast로 재연결 되고 있음을 알리기
            console.log(`🔄 웹소켓 재연결 시작 (정책: ${currentPolicy})`);
            reconnectAttemptsRef.current += 1;
            startSocket(joinCode, myName, menuId);
            wasConnectedBeforeBackground.current = false;
          }, delay);
        } else {
          // 즉시 재연결 시도
          console.log(`🔄 웹소켓 즉시 재연결 시도 (정책: ${currentPolicy})`);
          reconnectAttemptsRef.current += 1;
          startSocket(joinCode, myName, menuId);
          wasConnectedBeforeBackground.current = false;
        }
      } else {
        console.log(`📱 앱이 포그라운드로 전환됨 - 재연결 건너뜀 (정책: ${currentPolicy})`);
        wasConnectedBeforeBackground.current = false;
      }
    }

    // cleanup
    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
  }, [
    isVisible,
    isConnected,
    startSocket,
    stopSocket,
    shouldReconnect,
    getReconnectionDelay,
    currentPolicy,
    joinCode,
    myName,
    menuId,
  ]);

  const contextValue: WebSocketContextType = {
    startSocket,
    stopSocket,
    subscribe,
    send,
    isConnected,
    isVisible,
    client,
  };

  return <WebSocketContext.Provider value={contextValue}>{children}</WebSocketContext.Provider>;
};
