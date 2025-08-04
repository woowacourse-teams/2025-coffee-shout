import { ApiError, NetworkError } from './error';

const API_URL = process.env.REACT_APP_API_URL || 'http://api.coffee-shout.com';

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export type ApiRequestOptions<TData> = {
  method?: Method;
  headers?: Record<string, string>;
  body?: TData;
  params?: Record<string, string | number | boolean | null | undefined>;
  retry?: {
    count: number;
    delay: number;
  };
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
    params = null,
    retry = { count: 0, delay: 1000 },
  } = options;

  let requestUrl = API_URL + url;

  if (params) {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        searchParams.append(key, String(value));
      }
    });

    const queryString = searchParams.toString();
    if (queryString) {
      requestUrl += (url.includes('?') ? '&' : '?') + queryString;
    }
  }

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

        throw new ApiError(response.status, errorMessage, errorData);
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
          throw new NetworkError('네트워크 연결에 실패했습니다');
        }
      }

      throw error;
    }
  };

  return makeRequest();
};
