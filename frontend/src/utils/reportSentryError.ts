import { ApiError, NetworkError } from '@/apis/rest/error';
import * as Sentry from '@sentry/react';

export const reportApiError = (error: ApiError | NetworkError) => {
  if (error instanceof ApiError) {
    Sentry.captureException(error, {
      level: 'error',
      tags: {
        errorType: 'api',
        statusCode: error.status.toString(),
        errorCategory: error.status >= 500 ? 'server_error' : 'client_error',
      },
    });
  } else if (error instanceof NetworkError) {
    Sentry.captureException(error, {
      level: 'error',
      tags: {
        errorType: 'network',
        errorCategory: 'connection_error',
      },
    });
  }
};

type WebSocketErrorType = 'connection' | 'stomp' | 'subscription' | 'send' | 'parsing';

export const reportWebsocketError = (
  errorMessage: string,
  options?: {
    type?: WebSocketErrorType;
    extra?: Record<string, unknown>;
  }
) => {
  const { type = 'connection', extra } = options || {};

  Sentry.captureException(new Error(errorMessage), {
    level: 'error',
    tags: {
      errorType: 'websocket',
      websocketType: type,
      errorCategory: 'realtime_error',
    },
    extra: {
      timestamp: new Date().toISOString(),
      ...extra,
    },
  });
};
