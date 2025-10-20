import styled from '@emotion/styled';

export const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  height: 100%;
  max-width: 100%;
  justify-content: center;
  gap: 1rem;
`;

export const Header = styled.div`
  margin-bottom: 1.5rem;
`;

export const Content = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
`;

export const NamesContainer = styled.div`
  width: 100%;
  max-width: 90vw;
  overflow: hidden;
  margin-bottom: 1rem;
`;

export const WinnerName = styled.div<{ $slideDistance: number }>`
  display: flex;
  justify-content: ${({ $slideDistance }) => ($slideDistance > 0 ? 'flex-start' : 'center')};
  gap: 0.5rem;
  white-space: nowrap;
  animation: ${({ $slideDistance }) =>
    $slideDistance > 0 ? `slide 3400ms linear infinite 600ms` : 'none'};

  @keyframes slide {
    0% {
      transform: translateX(0);
    }
    100% {
      transform: translateX(${({ $slideDistance }) => -$slideDistance}px);
    }
  }
`;

export const ProbabilityWrapper = styled.div`
  display: flex;
  justify-content: center;
`;
