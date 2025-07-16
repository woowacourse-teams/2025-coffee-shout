import styled from '@emotion/styled';

export const Container = styled.h3<{ $color: string }>`
  ${({ theme }) => theme.typography.h3};
  color: ${({ $color }) => $color};
`;
