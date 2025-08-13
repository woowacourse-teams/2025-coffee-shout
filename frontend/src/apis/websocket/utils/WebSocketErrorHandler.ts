import { reportWebsocketError } from '@/apis/utils/reportSentryError';
import { Client, IFrame } from '@stomp/stompjs';
import { WebSocketErrorType } from '../constants/constants';

type WebSocketErrorContext = {
  type: WebSocketErrorType;
  url?: string;
  extra?: Record<string, unknown>;
};

type SubscriptionErrorParams = {
  url: string;
  errorMessage: string;
  messageBody?: string;
  onError?: (error: Error) => void;
};

type ConnectionRequiredErrorParams = {
  type: 'subscription' | 'send';
  url: string;
  isConnected: boolean;
  hasClient: boolean;
  onError?: (error: Error) => void;
};

type ParsingErrorParams = {
  url: string;
  originalError: unknown;
  messageBody: string;
  onError?: (error: Error) => void;
};

type SendErrorParams = {
  url: string;
  originalError: unknown;
  body: unknown;
  onError?: (error: Error) => void;
};

export class WebSocketErrorHandler {
  static handleError(
    message: string,
    context: WebSocketErrorContext,
    onError?: (error: Error) => void
  ): Error {
    console.error(message);
    reportWebsocketError(message, context);

    const error = new Error(message);
    onError?.(error);
    return error;
  }

  static handleStompError(frame: IFrame): Error {
    const errorDetails = {
      command: frame.command,
      message: frame.headers['message'] || '알 수 없는 STOMP 오류',
      body: frame.body,
    };

    const errorMessage = `STOMP 오류 [${errorDetails.command}]: ${errorDetails.message}`;

    return this.handleError(errorMessage, {
      type: 'stomp',
      extra: { errorDetails },
    });
  }

  static handleWebSocketError(event: Event, stompClient: Client): Error {
    const errorMessage = `WebSocket 연결 오류: ${event.type}`;

    return this.handleError(errorMessage, {
      type: 'connection',
      extra: {
        eventType: event.type,
        url: stompClient.webSocket?.url,
        readyState: stompClient.webSocket?.readyState,
      },
    });
  }

  static handleSubscriptionError({
    url,
    errorMessage,
    messageBody,
    onError,
  }: SubscriptionErrorParams): Error {
    const fullMessage = `구독 메시지 오류 (${url}): ${errorMessage}`;

    return this.handleError(
      fullMessage,
      {
        type: 'subscription',
        extra: { url, messageBody },
      },
      onError
    );
  }

  static handleConnectionRequiredError({
    type,
    url,
    isConnected,
    hasClient,
    onError,
  }: ConnectionRequiredErrorParams): Error {
    const TYPE_MESSAGE = {
      subscription: '구독',
      send: '메시지 전송',
    } as const;

    const errorMessage = `${TYPE_MESSAGE[type]} 실패 (${url}): WebSocket 연결 안됨`;

    return this.handleError(
      errorMessage,
      {
        type,
        extra: { url, isConnected, hasClient },
      },
      onError
    );
  }

  static handleParsingError({
    url,
    originalError,
    messageBody,
    onError,
  }: ParsingErrorParams): Error {
    const errorMessage = `JSON 파싱 실패 (${url}): ${originalError instanceof Error ? originalError.message : String(originalError)}`;

    return this.handleError(
      errorMessage,
      {
        type: 'parsing',
        extra: { url, messageBody, originalError: String(originalError) },
      },
      onError
    );
  }

  static handleSendError({ url, originalError, body, onError }: SendErrorParams): Error {
    const errorMessage = `메시지 전송 중 오류 (${url}): ${originalError instanceof Error ? originalError.message : String(originalError)}`;

    return this.handleError(
      errorMessage,
      {
        type: 'send',
        extra: { url, body: String(body), originalError: String(originalError) },
      },
      onError
    );
  }
}
