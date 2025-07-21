import styled from '@emotion/styled';

type WrapperProps = {
  $spinning?: boolean;
};

export const Container = styled.div`
  display: flex;
  flex: 1;
  justify-content: center;
  align-items: center;
`;

export const Wrapper = styled.div<WrapperProps>`
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background-color: #f0f0f0;
  border: 3px solid #ddd;
  margin: 2rem auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: #666;
  transition: box-shadow 0.2s;
  cursor: pointer;
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
`;
