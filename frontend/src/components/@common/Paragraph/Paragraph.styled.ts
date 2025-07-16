import styled from '@emotion/styled';

export const Container = styled.span<{ $color: string }>`
  ${({ theme }) => theme.typography.paragraph};
  color: ${({ $color }) => $color};
`;
