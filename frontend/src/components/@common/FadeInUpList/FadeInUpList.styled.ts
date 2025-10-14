import styled from '@emotion/styled';
import { keyframes } from '@emotion/react';

const fadeInUp = keyframes`
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
`;

export const ItemWrapper = styled.div<{
  $index: number;
  $staggerDelay: number;
  $animationDuration: number;
}>`
  animation: ${fadeInUp} ${({ $animationDuration }) => $animationDuration}ms ease-out forwards;
  animation-delay: ${({ $index, $staggerDelay }) => $index * $staggerDelay}ms;
  opacity: 0;
`;
