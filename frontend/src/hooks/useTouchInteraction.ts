import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { TouchState } from '@/types/touchState';
import { useState, useCallback } from 'react';

const TOUCH_DELAY_MS = 100;

export const useTouchInteraction = () => {
  const [touchState, setTouchState] = useState<TouchState>('idle');
  const isTouchDevice = checkIsTouchDevice();

  const handleTouchStart = useCallback(() => {
    if (!isTouchDevice) return;

    setTouchState('pressing');
  }, [isTouchDevice]);

  const handleTouchEnd = useCallback(() => {
    if (!isTouchDevice) return;

    setTouchState('releasing');
    setTimeout(() => {
      setTouchState('idle');
    }, TOUCH_DELAY_MS);
  }, [isTouchDevice]);

  return {
    touchState,
    handleTouchStart,
    handleTouchEnd,
  };
};
