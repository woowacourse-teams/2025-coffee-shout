import { css, keyframes } from '@emotion/react';

type Props = {
  activeColor: string;
  isTouching?: boolean;
};

const rippleKeyframes = keyframes`
  from {
    transform: scaleX(0);
    opacity: 0.4;
  }
  to {
    transform: scaleX(1);
    opacity: 1;
  }
`;

export const backgroundPressEffect = ({ activeColor, isTouching }: Props) => css`
  position: relative;
  overflow: hidden;

  &::before {
    z-index: -1;
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: ${activeColor};
    transform: scaleX(0);
    opacity: 0;
    border-radius: 12px;
  }

  ${isTouching &&
  css`
    &::before {
      animation: ${rippleKeyframes} 200ms ease-out forwards;
    }
  `}
`;
