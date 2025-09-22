import { useState, useCallback } from 'react';

const TOUCH_DELAY_MS = 100;

export const useTouchInteraction = () => {
  const [isTouching, setIsTouching] = useState(false);

  const startTouchPress = useCallback(() => {
    setIsTouching(true);
  }, []);

  const endTouchPress = useCallback((callback?: () => void) => {
    setTimeout(() => {
      setIsTouching(false);
      callback?.();
    }, TOUCH_DELAY_MS);
  }, []);

  return {
    isTouching,
    startTouchPress,
    endTouchPress,
  };
};
