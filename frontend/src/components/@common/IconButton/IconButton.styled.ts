import styled from '@emotion/styled';

type Props = {
  $isTouching: boolean;
};

export const Container = styled.button<Props>`
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  ${({ theme, $isTouching }) => {
    const baseColor = theme.color.gray[100];
    const activeColor = theme.color.gray[200];

    return `
      background-color: ${baseColor};
      
      /* 데스크톱: hover 효과 */
      @media (hover: hover) and (pointer: fine) {
        &:hover { background-color: ${activeColor}; }
      }
      
      /* 터치 디바이스: isPressed 상태로 제어 */
      ${$isTouching && `background-color: ${activeColor};`}
    `;
  }}
`;

export const Icon = styled.img`
  width: 25px;
  height: 25px;
  opacity: 0.5;
`;
