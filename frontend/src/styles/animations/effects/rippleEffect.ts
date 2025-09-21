import { ANIMATION_DURATION } from '@/constants/animation';
import { theme } from '@/styles/theme';
import { css } from '@emotion/react';

export const rippleEffect = (isTouching: boolean) => {
  const rippleColor = theme.color.gray[200];
  return css`
    position: relative;

    &::before {
      z-index: -1;
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 20px;
      height: 20px;
      background: ${rippleColor};
      border-radius: 50%;

      transform: translate(-50%, -50%) scale(${isTouching ? 1 : 0});
      opacity: ${isTouching ? 1 : 0};
      transition:
        transform ${ANIMATION_DURATION.RIPPLE}ms ease-out,
        opacity ${ANIMATION_DURATION.RIPPLE}ms ease-out;
    }
  `;
};
