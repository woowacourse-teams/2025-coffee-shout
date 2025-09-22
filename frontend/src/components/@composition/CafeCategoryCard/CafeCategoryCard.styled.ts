import styled from '@emotion/styled';
import { theme } from '@/styles/theme';

export const CategoryName = styled.span`
  ${theme.typography.paragraph}
  display: flex;
  align-items: start;
`;

export const Container = styled.button`
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;
`;
