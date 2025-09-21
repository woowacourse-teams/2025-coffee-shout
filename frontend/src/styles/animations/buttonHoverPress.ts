import { css } from '@emotion/react';
import { backgroundPressEffect } from './effects/backGroundPressEffect';
import { scalePressEffect } from './effects/scalePressEffect';

interface ButtonHoverPressProps {
  activeColor: string;
  isTouching: boolean;
  enableScale?: boolean;
}

export const buttonHoverPress = ({
  activeColor,
  isTouching,
  enableScale = true,
}: ButtonHoverPressProps) => {
  console.log('activeColor', activeColor);

  return css`
    /* 데스크톱: hover 효과 */
    @media (hover: hover) and (pointer: fine) {
      &:hover {
        background-color: ${activeColor};
      }
    }

    /* 터치 디바이스: isTouching 상태로 제어 */
    ${backgroundPressEffect({ activeColor, isTouching })}
    ${enableScale && scalePressEffect({ isTouching, scaleValue: 0.98 })}
  `;
};
