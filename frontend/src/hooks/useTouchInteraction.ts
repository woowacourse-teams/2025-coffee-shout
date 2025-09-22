import { TouchEvent, useState } from 'react';

export const useTouchInteraction = () => {
  const [isTouching, setIsTouching] = useState(false);

  const startTouchPress = () => {
    setIsTouching(true);
  };

  const endTouchPress = (
    callback?: (e: TouchEvent<HTMLButtonElement>) => void,
    e?: TouchEvent<HTMLButtonElement>
  ) => {
    setTimeout(() => {
      setIsTouching(false);
      callback?.(e as TouchEvent<HTMLButtonElement>);
    }, 100);
  };

  return {
    isTouching,
    startTouchPress,
    endTouchPress,
  };
};
