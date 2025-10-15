import styled from '@emotion/styled';

export const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  height: 100%;
  justify-content: center;
  gap: 2.5rem;
`;

export const Header = styled.div`
  margin-bottom: 1.5rem;
`;

export const Content = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

export const WinnerName = styled.div`
  margin-bottom: 1rem;
  animation: spinAndReveal 0.5s ease-out;
  transform-style: preserve-3d;
  perspective: 1000px;

  @keyframes spinAndReveal {
    0% {
      transform: rotateY(180deg) rotateX(0deg);
      opacity: 0;
    }
    25% {
      transform: rotateY(225deg) rotateX(0deg);
      opacity: 0.3;
    }
    50% {
      transform: rotateY(270deg) rotateX(0deg);
      opacity: 0.6;
    }
    75% {
      transform: rotateY(315deg) rotateX(0deg);
      opacity: 0.8;
    }
    100% {
      transform: rotateY(360deg) rotateX(0deg);
      opacity: 1;
    }
  }
`;

export const ProbabilityWrapper = styled.div`
  display: flex;
  justify-content: center;
`;
