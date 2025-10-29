import { useCallback } from 'react';
import { NavigateOptions, To, useNavigate } from 'react-router-dom';

export const useReplaceNavigate = () => {
  const navigate = useNavigate();

  return useCallback(
    (to: To | number, options?: NavigateOptions) => {
      if (typeof to === 'number') {
        navigate(to);
        return;
      }

      navigate(to, {
        replace: true,
        ...options,
        state: {
          fromInternal: true,
          ...options?.state,
        },
      });
    },
    [navigate]
  );
};
