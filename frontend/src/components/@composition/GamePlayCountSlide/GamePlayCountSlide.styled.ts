import styled from '@emotion/styled';

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
  animation: ${({ $slideDistance }) =>
    $slideDistance > 0 ? 'slideUp 4s linear infinite' : 'none'};

  @keyframes slideUp {
    0% {
      transform: translateY(0);
    }
    20% {
      transform: translateY(0);
    }
    60% {
      transform: translateY(-${({ $slideDistance }) => $slideDistance}px);
    }
    100% {
      transform: translateY(-${({ $slideDistance }) => $slideDistance}px);
    }
  }
`;
