import { ANIMATION_DURATION } from '@/constants/animation';
import { css } from '@emotion/react';

type Props = {
  activeColor: string;
  isTouching?: boolean;
};

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
    border-radius: 12px;

    transform: scaleX(${isTouching ? 1 : 0});
    opacity: ${isTouching ? 1 : 0};
    transform-origin: center;

    transition:
      ${isTouching ? `transform ${ANIMATION_DURATION.BACKGROUND_PRESS}ms ease-out` : 'none'},
      opacity ${ANIMATION_DURATION.BACKGROUND_PRESS}ms ease-out;
  }
`;
