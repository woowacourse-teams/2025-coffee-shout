import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { api } from './api';

type UseFetchOptions<T> = {
  endpoint: string;
  params?: Record<string, string | number | boolean | null | undefined>;
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
  const { endpoint, params, enabled = true, onSuccess, onError } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<Error | null>(null);

  const stableParams = useMemo(() => {
    if (!params) return {};

    const filteredParams: Record<string, string | number | boolean> = {};
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        filteredParams[key] = value;
      }
    });

    return filteredParams;
  }, [params]);

  const onSuccessRef = useRef(onSuccess);
  const onErrorRef = useRef(onError);

  const fetchData = useCallback(async () => {
    if (!enabled) return;

    try {
      setLoading(true);
      setError(null);
      const result = await api.get<T>(endpoint, { params: stableParams });
      setData(result);
      onSuccessRef.current?.(result);
    } catch (err) {
      setError(err as Error);
      onErrorRef.current?.(err as Error);
    } finally {
      setLoading(false);
    }
  }, [endpoint, stableParams, enabled]);

  useEffect(() => {
    if (enabled) {
      fetchData();
    }
  }, [enabled, fetchData]);

  return { data, loading, error, refetch: fetchData };
};

export default useFetch;
