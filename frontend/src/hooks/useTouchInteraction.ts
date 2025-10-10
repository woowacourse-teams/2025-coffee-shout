import { TouchState } from '@/types/touchState';
import { useState, useCallback, PointerEvent } from 'react';

const TOUCH_DELAY_MS = 100;

export const useTouchInteraction = () => {
  const [touchState, setTouchState] = useState<TouchState>('idle');

  const handleTouchDown = useCallback((e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    setTouchState('pressing');
  }, []);

  const handleTouchUp = useCallback((e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    setTouchState('releasing');
    setTimeout(() => {
      setTouchState('idle');
    }, TOUCH_DELAY_MS);
  }, []);

  return {
    touchState,
    handleTouchDown,
    handleTouchUp,
  };
};
