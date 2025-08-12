import { useCallback } from 'react';
import { useLocation } from 'react-router-dom';

export type ReconnectionPolicy = 'ALWAYS' | 'NEVER';

export const useReconnectionPolicy = () => {
  const location = useLocation();

  const getReconnectionPolicy = useCallback((): ReconnectionPolicy => {
    const pathname = location.pathname;

    // lobby 페이지에서만 재연결 허용
    if (pathname.includes('/lobby')) {
      return 'ALWAYS';
    }

    return 'NEVER';
  }, [location.pathname]);

  const shouldReconnect = useCallback(
    (wasConnectedBeforeBackground: boolean): boolean => {
      const policy = getReconnectionPolicy();

      // 기존 연결이 없었다면 재연결 시도하지 않음
      if (!wasConnectedBeforeBackground) {
        return false;
      }

      switch (policy) {
        case 'ALWAYS':
          return true;
        case 'NEVER':
          return false;
        default:
          return false;
      }
    },
    [getReconnectionPolicy]
  );

  const getReconnectionDelay = useCallback((): number => {
    const policy = getReconnectionPolicy();

    switch (policy) {
      case 'NEVER':
        return 0; // 재연결하지 않음
      case 'ALWAYS':
        return 1000; // 1초 지연
      default:
        return 1000;
    }
  }, [getReconnectionPolicy]);

  return {
    getReconnectionPolicy,
    shouldReconnect,
    getReconnectionDelay,
    currentPolicy: getReconnectionPolicy(),
  };
};
