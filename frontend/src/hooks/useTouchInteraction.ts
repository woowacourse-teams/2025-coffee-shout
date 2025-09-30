import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { TouchState } from '@/types/touchState';
import { useState, useCallback, TouchEvent, MouseEvent } from 'react';

const TOUCH_DELAY_MS = 100;

type UseTouchTransitionProps = {
  onClick?: (e: TouchEvent<HTMLButtonElement> | MouseEvent<HTMLButtonElement>) => void;
  isDisabled?: boolean;
};

export const useTouchInteraction = ({ onClick, isDisabled = false }: UseTouchTransitionProps) => {
  const [touchState, setTouchState] = useState<TouchState>('idle');
  const isTouchDevice = checkIsTouchDevice();

  const handleTouchStart = useCallback(
    (e: TouchEvent<HTMLButtonElement>) => {
      if (!isTouchDevice) return;
      if (isDisabled) return;

      e.preventDefault();
      setTouchState('pressing');
    },
    [isTouchDevice, isDisabled]
  );

  const handleTouchEnd = useCallback(
    (e: TouchEvent<HTMLButtonElement>) => {
      if (!isTouchDevice) return;
      if (isDisabled) return;

      e.preventDefault();
      setTouchState('releasing');
      setTimeout(() => {
        setTouchState('idle');
        onClick?.(e);
      }, TOUCH_DELAY_MS);
    },
    [isTouchDevice, isDisabled, onClick]
  );

  return {
    touchState,
    handleTouchStart,
    handleTouchEnd,
  };
};
