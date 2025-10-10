import type { PointerEvent } from 'react';

import { useCallback } from 'react';

import { useCancelablePress } from '@/hooks/useCancelablePress';
import { usePressAnimation } from '@/hooks/usePressAnimation';

type Props = {
  onClick?: () => void;
};

export const useButtonInteraction = ({ onClick }: Props = {}) => {
  const {
    touchState,
    onPointerDown: onPointerDownAnimation,
    onPointerUp: onPointerUpAnimation,
    onPointerCancel: onPointerCancelAnimation,
  } = usePressAnimation();

  const {
    moved,
    onPointerDown: onPointerDownCancel,
    onPointerMove: onPointerMoveCancel,
    onPointerCancel: onPointerCancelPress,
    onPointerUp: onPointerUpCancel,
  } = useCancelablePress({
    onClick,
  });

  const onPointerDown = useCallback(
    (e: PointerEvent<HTMLElement>) => {
      onPointerDownAnimation(e);
      onPointerDownCancel(e);
    },
    [onPointerDownAnimation, onPointerDownCancel]
  );

  const onPointerMove = useCallback(
    (e: PointerEvent<HTMLElement>) => {
      onPointerMoveCancel(e);

      if (moved.current && touchState === 'pressing') {
        onPointerCancelAnimation(e);
      }
    },
    [onPointerMoveCancel, moved, touchState, onPointerCancelAnimation]
  );

  const onPointerCancel = useCallback(
    (e: PointerEvent<HTMLElement>) => {
      suppressNextClick();
      onPointerCancelAnimation(e);
      onPointerCancelPress(e);
    },
    [onPointerCancelAnimation, onPointerCancelPress]
  );

  const onPointerUp = useCallback(
    (e: PointerEvent<HTMLElement>) => {
      suppressNextClick();
      onPointerUpAnimation(e);
      onPointerUpCancel(e);
    },
    [onPointerUpAnimation, onPointerUpCancel]
  );

  return {
    touchState,
    onPointerDown,
    onPointerMove,
    onPointerCancel,
    onPointerUp,
  };
};

const suppressNextClick = () => {
  const handler = (e: MouseEvent) => {
    e.stopPropagation();
    e.preventDefault();
  };

  document.addEventListener('click', handler, true); // capture 단계에서 차단
  requestAnimationFrame(() => {
    document.removeEventListener('click', handler, true);
  });
};
