import styled from '@emotion/styled';

type WrapperProps = {
  $isSpinning?: boolean;
  $finalRotation?: number;
};

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
`;

export const Wrapper = styled.div<WrapperProps>`
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background-color: ${({ theme }) => theme.color.point[100]};
  margin: 2rem auto;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;

  --final-rotation: ${({ $finalRotation }) => $finalRotation ?? 0}deg;

  ${({ $isSpinning }) =>
    $isSpinning &&
    `
      animation: spin 3s cubic-bezier(0.33, 1, 0.68, 1);
      animation-fill-mode: forwards;
    `}

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(calc(1080deg + var(--final-rotation)));
    }
  }
`;

export const PlayerNameText = styled.text`
  fill: ${({ theme }) => theme.color.point[100]};
  font-size: 12px;
  font-weight: bold;
`;

export const Pin = styled.div`
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-top: 30px solid ${({ theme }) => theme.color.gray[500]};
  border-radius: 4px;
  position: absolute;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
`;
