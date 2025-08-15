import styled from '@emotion/styled';

type Props = {
  $size?: string;
  $isActive?: boolean;
};

export const Container = styled.div<Props>`
  position: relative;
  width: ${({ $size }) => $size || '2rem'};
  height: ${({ $size }) => $size || '2rem'};
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const ProgressRing = styled.svg`
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
`;

export const BackgroundCircle = styled.circle`
  stroke: ${({ theme }) => theme.color.gray[200]};
  stroke-width: 10;
`;

export const ProgressCircle = styled.circle<Props>`
  stroke: ${({ theme }) => theme.color.point[400]};
  stroke-width: 10;
  stroke-linecap: round;
  transition: ${({ $isActive }) => ($isActive ? 'stroke-dashoffset 1.1s linear' : 'none')};
`;

export const CountText = styled.span`
  color: ${({ theme }) => theme.color.gray[700]};
  font-size: 0.75rem; // ~12px
  font-weight: 700;
  text-align: center;
`;
