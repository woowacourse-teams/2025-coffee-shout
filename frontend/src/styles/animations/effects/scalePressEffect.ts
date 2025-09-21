import { css, keyframes } from '@emotion/react';

interface ScalePressEffectProps {
  isTouching: boolean;
  scaleValue?: number;
  duration?: number;
}

export const scalePressEffect = ({
  isTouching,
  scaleValue = 0.98,
  duration = 150,
}: ScalePressEffectProps) => {
  const pressDownKeyframes = keyframes`
    0% { transform: scale(1); }
    100% { transform: scale(${scaleValue}); }
  `;

  if (!isTouching) {
    return css`
      transition: transform ${duration}ms ease-in-out;
      transform: scale(1);
    `;
  }

  return css`
    animation: ${pressDownKeyframes} ${duration}ms ease-out forwards;
    transform: scale(${scaleValue});
    transition: transform ${duration}ms ease-in-out;
  `;
};
