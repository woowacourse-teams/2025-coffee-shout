import { useState } from 'react';

export const useTouchInteraction = () => {
  const [isTouching, setIsTouching] = useState(false);

  const startTouchPress = () => {
    setIsTouching(true);
  };

  const endTouchPress = () => {
    setIsTouching(false);
  };

  return {
    isTouching,
    startTouchPress,
    endTouchPress,
  };
};
