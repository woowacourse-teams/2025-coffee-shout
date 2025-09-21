import { css } from '@emotion/react';

interface ScalePressEffectProps {
  isTouching?: boolean;
  scaleValue?: number;
}

export const scalePressEffect = ({ isTouching, scaleValue = 0.98 }: ScalePressEffectProps) => css`
  transition: transform 0.2s ease-in-out;
  ${isTouching && `transform: scale(${scaleValue});`}
`;
