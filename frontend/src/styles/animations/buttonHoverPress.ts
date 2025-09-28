import { css } from '@emotion/react';
import { backgroundPressEffect } from './effects/backGroundPressEffect';
import { scalePressEffect } from './effects/scalePressEffect';

type TouchState = 'idle' | 'pressing' | 'releasing';

interface ButtonHoverPressProps {
  activeColor: string;
  touchState: TouchState;
  enableScale?: boolean;
}

export const buttonHoverPress = ({
  activeColor,
  touchState,
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

    /* 터치 디바이스: touchState 상태로 제어 */
    ${backgroundPressEffect({ activeColor, touchState })}
    ${enableScale && scalePressEffect({ touchState, scaleValue: 0.98 })}
  `;
};
