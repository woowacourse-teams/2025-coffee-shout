import styled from '@emotion/styled';

export const Container = styled.h1<{ $color: string }>`
  ${({ theme }) => theme.typography.h1};
  color: ${({ $color }) => $color};
`;
