import styled from '@emotion/styled';

type FlipperProps = {
  flipped: boolean;
};

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

export const FlipWheelWrapper = styled.div`
  position: relative;
  perspective: 1000px;
  width: 100%;
  height: 100%;
`;

export const Flipper = styled.div<FlipperProps>`
  position: absolute;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.8s ease-in-out;
  transform-origin: center;
  transform: ${({ flipped }) => (flipped ? 'rotateY(180deg)' : 'rotateY(0deg)')};
`;
export const Front = styled.div`
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  transform: rotateY(180deg);
`;

export const Back = styled.div`
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  transform: rotateY(0deg);
`;
