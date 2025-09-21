import { theme } from '@/styles/theme';
import { css, keyframes } from '@emotion/react';

const rippleKeyframes = keyframes`
  from {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0.4;
  }
  to {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
`;

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
      transform: scale(0);
      opacity: 0;
      border-radius: 50%;
    }

    ${isTouching &&
    css`
      &::before {
        animation: ${rippleKeyframes} 200ms ease-out forwards;
      }
    `}
  `;
};
