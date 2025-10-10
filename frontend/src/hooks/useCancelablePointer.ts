import { useRef, PointerEvent } from 'react';

type Props = {
  onClick?: () => void;
  cancelThreshold?: number;
};

export const useCancelablePointer = ({ onClick, cancelThreshold = 10 }: Props) => {
  const startY = useRef(0);
  const startX = useRef(0);
  const moved = useRef(false);

  const handlePointerDown = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    startY.current = e.clientY;
    startX.current = e.clientX;
    moved.current = false;
  };

  const handlePointerMove = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch') return;

    const diffY = Math.abs(e.clientY - startY.current);
    const diffX = Math.abs(e.clientX - startX.current);
    if (diffY > cancelThreshold || diffX > cancelThreshold) {
      moved.current = true;
    }
  };

  const handlePointerUp = (e: PointerEvent<HTMLElement>) => {
    if (e.pointerType !== 'touch' || !moved.current) {
      onClick?.();
    }
  };

  return {
    handlePointerDown,
    handlePointerMove,
    handlePointerUp,
  };
};
