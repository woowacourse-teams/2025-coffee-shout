import type { PointerEvent } from 'react';

import { useRef } from 'react';

type Props = {
  onClick?: () => void;
  cancelThreshold?: number;
};

export const useCancelablePress = ({ onClick, cancelThreshold = 10 }: Props) => {
  const startY = useRef(0);
  const startX = useRef(0);
  const moved = useRef(false);

  const onPointerDown = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    startY.current = e.clientY;
    startX.current = e.clientX;
    moved.current = false;
  };

  const onPointerMove = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    const diffY = Math.abs(e.clientY - startY.current);
    const diffX = Math.abs(e.clientX - startX.current);
    if (diffY > cancelThreshold || diffX > cancelThreshold) {
      moved.current = true;
    }
  };

  const onPointerUp = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    if (!moved.current) {
      onClick?.();
    }
  };

  return {
    onPointerDown,
    onPointerMove,
    onPointerUp,
  };
};

