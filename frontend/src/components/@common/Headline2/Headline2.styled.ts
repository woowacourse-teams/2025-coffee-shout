import styled from '@emotion/styled';

export const Container = styled.h2<{ $color: string }>`
  ${({ theme }) => theme.typography.h2};
  color: ${({ $color }) => $color};
`;
