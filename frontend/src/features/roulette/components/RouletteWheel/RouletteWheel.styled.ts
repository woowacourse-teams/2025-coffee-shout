import styled from '@emotion/styled';

type WrapperProps = {
  $spinning?: boolean;
};

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  margin-bottom: 5rem;
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
  ${({ $spinning }) =>
    $spinning &&
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

export const CenterImage = styled.img`
  width: 64px;
  height: 64px;
  z-index: 10;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
`;
