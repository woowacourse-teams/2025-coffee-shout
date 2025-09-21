import { css } from '@emotion/react';

interface ScalePressEffectProps {
  isTouching: boolean;
  scaleValue?: number;
  duration?: number;
}

export const scalePressEffect = ({
  isTouching,
  scaleValue = 0.98,
  duration = 80,
}: ScalePressEffectProps) => {
  return css`
    transform: scale(${isTouching ? scaleValue : 1});
    transition: transform ${duration}ms ease-out;
  `;
};
