import { css } from '@emotion/react';

interface BackgroundPressEffectProps {
  activeColor: string;
  isTouching?: boolean;
}

export const backgroundPressEffect = ({
  activeColor,
  isTouching,
}: BackgroundPressEffectProps) => css`
  transition: background-color 0.2s ease-in-out;

  ${isTouching && `background-color: ${activeColor};`}
`;
