import styled from '@emotion/styled';

export const Player = styled.div<{ $isMe: boolean; $x: number; $myX: number }>`
  transform: ${({ $isMe, $x, $myX }) => {
    if ($isMe) return 'translateX(0)';
    const relativeX = $x - $myX;
    return `translateX(${relativeX}px)`;
  }};
  transition: transform 0.3s ease-in-out;
`;

export const RotatingWrapper = styled.div<{ $speed: number }>`
  animation: spin ${({ $speed }) => 10 / $speed}s linear infinite;

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
`;
