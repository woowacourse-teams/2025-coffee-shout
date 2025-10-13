import { useCallback, useRef, useState } from 'react';
import { api } from './api';

type UseLazyFetchOptions<T> = {
  endpoint: string;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
};

type UseLazyFetchReturn<T> = {
  data: T | undefined;
  loading: boolean;
  error: Error | null;
  execute: () => Promise<T | undefined>;
};

const useLazyFetch = <T>(options: UseLazyFetchOptions<T>): UseLazyFetchReturn<T> => {
  const { endpoint, onSuccess, onError } = options;

  const [data, setData] = useState<T | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  const execute = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await api.get<T>(endpoint);
      setData(result);
      onSuccessRef.current?.(result);
      return result;
    } catch (error) {
      setError(error as Error);
      onErrorRef.current?.(error as Error);
    } finally {
      setLoading(false);
    }
  }, [endpoint]);

  if (error) throw error;

  return { data, loading, error, execute };
};

export default useLazyFetch;
