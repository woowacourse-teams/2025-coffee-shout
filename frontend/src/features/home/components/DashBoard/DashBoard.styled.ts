import styled from '@emotion/styled';

export const CarouselContainer = styled.div`
  position: relative;
  width: 100%;
  height: 100%;
`;

export const SlideWrapper = styled.div<{ $isTransitioning: boolean }>`
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  animation: ${({ $isTransitioning }) =>
    $isTransitioning ? 'fadeOut 600ms ease-in-out forwards' : 'fadeIn 600ms ease-in-out forwards'};

  @keyframes fadeIn {
    0% {
      opacity: 0;
      transform: scale(0.8);
    }
    100% {
      opacity: 1;
      transform: scale(1);
    }
  }

  @keyframes fadeOut {
    0% {
      opacity: 1;
      transform: scale(1);
    }
    100% {
      opacity: 0;
      transform: scale(0.8);
    }
  }
`;
