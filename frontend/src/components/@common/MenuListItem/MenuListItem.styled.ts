import styled from '@emotion/styled';

export const Container = styled.button`
  display: flex;
  align-items: center;
  width: 100%;
  height: 40px;
  border-bottom: 1px solid ${({ theme }) => theme.color.gray[200]};
  padding-left: 16px;
  background: none;
  cursor: pointer;
`;

export const Text = styled.span`
  ${({ theme }) => theme.typography.paragraph}
  color: ${({ theme }) => theme.color.gray[800]};
`;
