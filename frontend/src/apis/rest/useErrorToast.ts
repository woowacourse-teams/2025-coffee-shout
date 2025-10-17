import { useCallback } from 'react';
import useToast from '@/components/@common/Toast/useToast';
import { getErrorInfo } from '@/utils/errorMessages';

export const useErrorToast = () => {
  const { showToast } = useToast();

  const showErrorToast = useCallback(
    (error: Error): void => {
      const errorMessage = getErrorInfo(error).message;

      showToast({
        type: 'error',
        message: errorMessage,
      });
    },
    [showToast]
  );

  return { showErrorToast };
};
