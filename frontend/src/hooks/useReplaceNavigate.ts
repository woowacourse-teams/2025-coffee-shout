import { useCallback } from 'react';
import { useNavigate, NavigateOptions } from 'react-router-dom';

export const useReplaceNavigate = () => {
  const navigate = useNavigate();

  return useCallback(
    (to: string | number, options?: NavigateOptions) => {
      if (typeof to === 'number') {
        navigate(to);
        return;
      }

      navigate(to, {
        replace: true,
        state: { fromInternal: true },
        ...options,
      });
    },
    [navigate]
  );
};
