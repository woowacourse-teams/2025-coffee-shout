import styled from '@emotion/styled';

export const Container = styled.button`
  display: flex;
  align-items: center;
  width: 100%;
  height: 50px;
  border-bottom: 1px solid ${({ theme }) => theme.color.gray[200]};
  padding-left: 16px;
  background: none;
  cursor: pointer;
`;
