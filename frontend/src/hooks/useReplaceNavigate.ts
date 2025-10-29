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
        ...options,
        state: {
          ...options?.state,
          fromInternal: true,
        },
        replace: options?.replace !== undefined ? options.replace : true,
      });
    },
    [navigate]
  );
};
