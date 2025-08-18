export const WEBSOCKET_CONFIG = {
  MAX_RECONNECT_ATTEMPTS: 5,
  RECONNECT_DELAY_MS: 1000,
  TOPIC_PREFIX: '/topic',
  APP_PREFIX: '/app',
} as const;

export type WebSocketSuccess<T> = {
  success: true;
  data: T;
  errorMessage: null;
};

export type WebSocketError = {
  success: false;
  data: null;
  errorMessage: string;
};

export type WebSocketErrorOptions = {
  type?: WebSocketErrorType;
  extra?: Record<string, unknown>;
};

export type WebSocketMessage<T> = WebSocketSuccess<T> | WebSocketError;

export type WebSocketErrorType = 'stomp' | 'connection' | 'subscription' | 'send' | 'parsing';
