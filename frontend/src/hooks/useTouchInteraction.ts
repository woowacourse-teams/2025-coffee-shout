import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';
import { useState, useCallback, TouchEvent, MouseEvent } from 'react';

const TOUCH_DELAY_MS = 100;

type UseTouchTransitionProps = {
  onClick: (e: TouchEvent<HTMLButtonElement> | MouseEvent<HTMLButtonElement>) => void;
  isDisabled?: boolean;
};

export const useTouchInteraction = ({ onClick, isDisabled = false }: UseTouchTransitionProps) => {
  const [isTouching, setIsTouching] = useState(false);
  const isTouchDevice = checkIsTouchDevice();

  const handleTouchStart = useCallback(
    (e: TouchEvent<HTMLButtonElement>) => {
      if (!isTouchDevice) return;
      if (isDisabled) return;

      e.preventDefault();
      setIsTouching(true);
    },
    [isTouchDevice, isDisabled]
  );

  const handleTouchEnd = useCallback(
    (e: TouchEvent<HTMLButtonElement>) => {
      if (!isTouchDevice) return;
      if (isDisabled) return;

      e.preventDefault();
      setTimeout(() => {
        setIsTouching(false);
        onClick(e);
      }, TOUCH_DELAY_MS);
    },
    [isTouchDevice, isDisabled, onClick]
  );

  return {
    isTouching,
    handleTouchStart,
    handleTouchEnd,
  };
};
