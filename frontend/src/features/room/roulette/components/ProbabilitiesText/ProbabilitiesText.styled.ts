import styled from '@emotion/styled';

type ProbabilityTextProps = { $isProbabilitiesLoading: boolean };
type ProbabilityChangeProps = { $isPositive: boolean };

export const ProbabilityText = styled.div<ProbabilityTextProps>`
  text-align: center;
  padding-bottom: 2rem;
  opacity: ${({ $isProbabilitiesLoading }) => ($isProbabilitiesLoading ? 0 : 1)};
  transition: opacity 0.3s ease-in-out;
`;

export const ProbabilityChange = styled.span<ProbabilityChangeProps>`
  color: ${({ $isPositive }) => ($isPositive ? '#FF0000' : '#0066FF')};
  font-weight: bold;
`;
