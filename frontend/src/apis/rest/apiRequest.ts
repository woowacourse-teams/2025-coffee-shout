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
  displayMode?: ErrorDisplayMode;
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
    displayMode,
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

        // 지정된 display가 있으면 지정된 모드를, 없으면 get일 때는 fallback, 나머지는 toast
        const display = displayMode || (method === 'GET' ? 'fallback' : 'toast');
        const apiError = new ApiError(response.status, errorMessage, errorData, display, method);
        reportApiError(apiError);
        throw apiError;
      }

      return await response.json();
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
          const networkError = new NetworkError(error.message);
          reportApiError(networkError);
          throw networkError;
        }
      }

      throw error;
    }
  };

  return makeRequest();
};
