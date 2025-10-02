import { useCallback, useEffect, useRef, useState } from 'react';
import { api } from './api';
import { ErrorDisplayMode } from './error';

type UseFetchOptions<T> = {
  endpoint: string;
  enabled?: boolean;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
  displayMode?: ErrorDisplayMode;
};

type UseFetchReturn<T> = {
  data: T | undefined;
  loading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
};

const useFetch = <T>(options: UseFetchOptions<T>): UseFetchReturn<T> => {
  const { endpoint, enabled = true, onSuccess, onError, displayMode } = options;

  const [data, setData] = useState<T | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<Error | null>(null);

  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  const fetchData = useCallback(async () => {
    if (!enabled) return;

    try {
      setLoading(true);
      setError(null);
      const result = await api.get<T>(endpoint, { displayMode });
      setData(result);
      onSuccessRef.current?.(result);
    } catch (error) {
      setError(error as Error);
      onErrorRef.current?.(error as Error);
    } finally {
      setLoading(false);
    }
  }, [endpoint, enabled, displayMode]);

  useEffect(() => {
    if (enabled) {
      fetchData();
    }
  }, [enabled, fetchData]);

  if (error) throw error;

  return { data, loading, error, refetch: fetchData };
};

export default useFetch;
