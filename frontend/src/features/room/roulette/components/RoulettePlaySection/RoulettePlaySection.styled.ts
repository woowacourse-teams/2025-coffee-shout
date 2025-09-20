import styled from '@emotion/styled';

type Props = { isPositive: boolean };

export const Container = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

export const RouletteWheelWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  width: 300px;
  height: 364px;
`;

export const ProbabilityText = styled.div`
  text-align: center;
  padding-bottom: 2rem;
`;

export const ProbabilityChange = styled.span<Props>`
  color: ${({ isPositive }) => (isPositive ? '#FF0000' : '#0066FF')};
  font-weight: bold;
`;
