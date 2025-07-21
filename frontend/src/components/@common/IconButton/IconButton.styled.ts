import styled from '@emotion/styled';

export const Container = styled.button`
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 4px;
  background-color: ${({ theme }) => theme.color.gray[100]};
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.color.gray[200]};
  }
`;

export const Icon = styled.img`
  width: 25px;
  height: 25px;
  opacity: 0.5;
`;
