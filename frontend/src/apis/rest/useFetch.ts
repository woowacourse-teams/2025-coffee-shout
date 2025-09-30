import { useCallback, useEffect, useRef, useState } from 'react';
import { api } from './api';

type UseFetchOptions<T> = {
  endpoint: string;
  enabled?: boolean;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
};

type UseFetchReturn<T> = {
  data: T | null;
  loading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
};

const useFetch = <T>(options: UseFetchOptions<T>): UseFetchReturn<T> => {
  const { endpoint, enabled = true, onSuccess, onError } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<Error | null>(null);

  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  const fetchData = useCallback(async () => {
    if (!enabled) return;

    try {
      setLoading(true);
      setError(null);
      const result = await api.get<T>(endpoint);
      setData(result);
      onSuccessRef.current?.(result);
    } catch (error) {
      setError(error as Error);
      onErrorRef.current?.(error as Error);
    } finally {
      setLoading(false);
    }
  }, [endpoint, enabled]);

  useEffect(() => {
    if (enabled) {
      fetchData();
    }
  }, [enabled, fetchData]);

  return { data, loading, error, refetch: fetchData };
};

export default useFetch;
