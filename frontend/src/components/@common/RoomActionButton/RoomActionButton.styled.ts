import styled from '@emotion/styled';

type Props = {
  $isTouching: boolean;
};

export const Container = styled.button<Props>`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;

  width: 100%;
  height: 130px;
  border-radius: 12px;
  padding: 28px 20px;

  ${({ theme, $isTouching }) => {
    const baseColor = theme.color.gray[50];
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

export const NextStepIcon = styled.img`
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
`;

export const DescriptionBox = styled.div`
  display: flex;
  align-items: flex-start;
`;
