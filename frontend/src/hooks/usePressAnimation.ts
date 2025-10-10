import type { PointerEvent } from 'react';

import { useState, useCallback } from 'react';

import type { TouchState } from '@/types/touchState';

const TOUCH_DELAY_MS = 100;

export const usePressAnimation = () => {
  const [touchState, setTouchState] = useState<TouchState>('idle');

  const onPointerDown = useCallback((e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    setTouchState('pressing');
  }, []);

  const onPointerUp = useCallback((e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    setTouchState('releasing');
    setTimeout(() => {
      setTouchState('idle');
    }, TOUCH_DELAY_MS);
  }, []);

  return {
    touchState,
    onPointerDown,
    onPointerUp,
  };
};

