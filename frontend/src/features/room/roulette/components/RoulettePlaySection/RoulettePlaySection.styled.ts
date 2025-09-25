import styled from '@emotion/styled';

type ProbabilityChangeProps = { isPositive: boolean };
type ProbabilityTextProps = { $isProbabilitiesLoading: boolean };

export const Container = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

export const RouletteWheelWrapper = styled.div`
  display: flex;
  width: 300px;
  height: 364px;
`;

export const ProbabilityText = styled.div<ProbabilityTextProps>`
  text-align: center;
  padding-bottom: 2rem;
  opacity: ${({ $isProbabilitiesLoading }) => ($isProbabilitiesLoading ? 0 : 1)};
  transition: opacity 0.3s ease-in-out;
`;

export const ProbabilityChange = styled.span<ProbabilityChangeProps>`
  color: ${({ isPositive }) => (isPositive ? '#FF0000' : '#0066FF')};
  font-weight: bold;
`;
