import styled from '@emotion/styled';

export const Container = styled.h4<{ $color: string }>`
  ${({ theme }) => theme.typography.h4};
  color: ${({ $color }) => $color};
`;
