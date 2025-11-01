import { ApiError, ErrorDisplayMode, NetworkError } from './error';
import { reportApiError } from '@/apis/utils/reportSentryError';

const API_URL = process.env.API_URL;

export type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export type ApiRequestOptions<TData> = {
  method?: Method;
  headers?: Record<string, string>;
  body?: TData;
  retry?: {
    count: number;
    delay: number;
  };
  errorDisplayMode?: ErrorDisplayMode;
};

export type ApiConfig = {
  method: Method;
  headers: Record<string, string>;
  body: string | null;
};

export const apiRequest = async <T, TData>(
  url: string,
  options: ApiRequestOptions<TData> = {}
): Promise<T> => {
  const {
    method = 'GET',
    headers = {},
    body = null,
    retry = { count: 0, delay: 1000 },
    errorDisplayMode = options.errorDisplayMode || (method === 'GET' ? 'fallback' : 'toast'),
  } = options;

  let requestUrl = API_URL + url;

  const defaultHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  const parsedBody = body ? JSON.stringify(body) : null;

  const makeRequest = async (retryCount = 0): Promise<T> => {
    try {
      const fetchOptions: ApiConfig = {
        method: method,
        headers: defaultHeaders,
        body: method !== 'GET' && parsedBody ? parsedBody : null,
      };

      const response = await fetch(requestUrl, fetchOptions);

      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP ${response.status} Error`;

        try {
          const contentType = response.headers.get('content-type');

          if (
            contentType &&
            (contentType.includes('application/json') ||
              contentType.includes('application/problem+json'))
          ) {
            errorData = await response.json();
            errorMessage = errorData.detail;
          } else {
            const textError = await response.text();
            errorMessage = textError || errorMessage;
          }
        } catch (parseError) {
          console.warn('응답 메시지 파싱 실패', parseError);
        }

        const apiError = new ApiError({
          status: response.status,
          message: errorMessage,
          data: errorData,
          displayMode: errorDisplayMode,
        });
        reportApiError(apiError);
        throw apiError;
      }

      if (response.status === 204) {
        return {} as T;
      }

      const text = await response.text();
      if (!text) {
        return {} as T;
      }

      return JSON.parse(text);
    } catch (error) {
      if (retryCount < retry.count) {
        console.warn(`재시도 중 (${retryCount + 1}/${retry.count})`);
        await new Promise((resolve) => setTimeout(resolve, retry.delay));

        return makeRequest(retryCount + 1);
      }

      if (error instanceof ApiError) {
        throw error;
      }

      if (error instanceof TypeError) {
        if (error.message.includes('fetch') || error.message.includes('Failed to fetch')) {
          const networkError = new NetworkError({
            message: error.message,
            displayMode: errorDisplayMode,
          });
          reportApiError(networkError);
          throw networkError;
        }
      }

      throw error;
    }
  };

  return makeRequest();
};
