import { ANIMATION_DURATION, ANIMATION_SCALE } from '@/constants/animation';
import { css } from '@emotion/react';

interface ScalePressEffectProps {
  isTouching: boolean;
  scaleValue?: number;
  duration?: number;
}

export const scalePressEffect = ({
  isTouching,
  scaleValue = ANIMATION_SCALE.SCALE_PRESS,
  duration = ANIMATION_DURATION.SCALE_PRESS,
}: ScalePressEffectProps) => {
  return css`
    transform: scale(${isTouching ? scaleValue : 1});
    transition: transform ${duration}ms ease-out;
  `;
};
