import { useRef, TouchEvent } from 'react';

type Props = {
  onClick?: () => void;
  cancelThreshold?: number;
};

export const useCancelableTouch = ({ onClick, cancelThreshold = 10 }: Props) => {
  const startY = useRef(0);
  const startX = useRef(0);
  const moved = useRef(false);

  const handleTouchStart = (e: TouchEvent<HTMLElement>) => {
    startY.current = e.touches[0].clientY;
    startX.current = e.touches[0].clientX;
    moved.current = false;
  };

  const handleTouchMove = (e: TouchEvent<HTMLElement>) => {
    const diffY = Math.abs(e.touches[0].clientY - startY.current);
    const diffX = Math.abs(e.touches[0].clientX - startX.current);
    if (diffY > cancelThreshold || diffX > cancelThreshold) {
      moved.current = true;
    }
  };

  const handleTouchEnd = () => {
    if (!moved.current) onClick?.();
  };

  return {
    handleTouchStart,
    handleTouchMove,
    handleTouchEnd,
  };
};
