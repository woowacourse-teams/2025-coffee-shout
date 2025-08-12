import styled from '@emotion/styled';
import { keyframes } from '@emotion/react';

const slideInFromRight = keyframes`
  from {
    transform: translateX(30%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
`;

const slideOutToLeft = keyframes`
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(-30%);
    opacity: 0;
  }
`;

export const Container = styled.div`
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  &.slide-first {
    animation:
      ${slideInFromRight} 0.4s ease-out 0s forwards,
      ${slideOutToLeft} 0.4s ease-in 2s forwards;
  }

  &.slide-second {
    opacity: 0;
    transform: translateX(100%);
    animation: ${slideInFromRight} 0.4s ease-out 2s forwards;
  }
`;

export const TextWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
`;

export const ImageWrapper = styled.div`
  width: 100%;
  height: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
`;
