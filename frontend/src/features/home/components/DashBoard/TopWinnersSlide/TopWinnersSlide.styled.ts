import styled from '@emotion/styled';
import { ANIMATION_DURATION } from '@/constants/animation';

export const SlideContainer = styled.div`
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
`;

export const Wrapper = styled.div<{ $slideDistance: number }>`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;

  ${({ $slideDistance }) =>
    $slideDistance > 0 &&
    `
      animation: slideUp-${$slideDistance} ${ANIMATION_DURATION.DASHBOARD_DISPLAY}ms linear infinite forwards;

      @keyframes slideUp-${$slideDistance} {
        0% {
          transform: translateY(0);
        }
        20% {
          transform: translateY(0);
        }
        60% {
          transform: translateY(-${$slideDistance}px);
        }
        100% {
          transform: translateY(-${$slideDistance}px);
        }
      }
    `}
`;
