import { useCallback, useRef, useState } from 'react';
import { api } from './api';

type UseLazyFetchOptions<T> = {
  endpoint: string;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
};

type UseLazyFetchReturn<T> = {
  data: T | null;
  loading: boolean;
  error: Error | null;
  execute: (params?: Record<string, string | number | boolean>) => Promise<T | null>;
};

const useLazyFetch = <T>(options: UseLazyFetchOptions<T>): UseLazyFetchReturn<T> => {
  const { endpoint, onSuccess, onError } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  const execute = useCallback(
    async (params?: Record<string, string | number | boolean>) => {
      try {
        setLoading(true);
        setError(null);
        const result = await api.get<T>(endpoint, { params: params || {} });
        setData(result);
        onSuccessRef.current?.(result);
        return result;
      } catch (err) {
        setError(err as Error);
        onErrorRef.current?.(err as Error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [endpoint]
  );

  return { data, loading, error, execute };
};

export default useLazyFetch;
