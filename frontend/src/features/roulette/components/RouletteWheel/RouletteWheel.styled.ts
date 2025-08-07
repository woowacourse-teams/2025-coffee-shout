import styled from '@emotion/styled';

type WrapperProps = {
  $isSpinning?: boolean;
};

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
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
  ${({ $isSpinning }) =>
    $isSpinning &&
    `
      animation: spin 3s cubic-bezier(0.33, 1, 0.68, 1);
    `}

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(1080deg);
    }
  }
`;

export const PlayerNameText = styled.text`
  fill: ${({ theme }) => theme.color.point[100]};
  font-size: 12px;
  font-weight: bold;
`;
