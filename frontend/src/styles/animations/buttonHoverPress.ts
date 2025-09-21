import { css } from '@emotion/react';

interface ButtonHoverPressProps {
  activeColor: string;
  isTouching?: boolean;
}

export const buttonHoverPress = ({ activeColor, isTouching }: ButtonHoverPressProps) => {
  console.log('activeColor', activeColor);
  return css`
    transition:
      background-color 0.2s ease-in-out,
      transform 0.2s ease-in-out;

    /* 데스크톱: hover 효과 */
    @media (hover: hover) and (pointer: fine) {
      &:hover {
        background-color: ${activeColor};
      }
    }

    /* 터치 디바이스: isTouching 상태로 제어 */
    ${isTouching &&
    ` background-color: ${activeColor};
      transform: scale(0.98);
    `}
  `;
};
