import styled from '@emotion/styled';

const TRANSITION_DURATION_MS = 300;

export const Player = styled.div<{ $isMe: boolean; $x: number; $myX: number }>`
  transform: ${({ $isMe, $x, $myX }) => {
    if ($isMe) return 'translateX(0)';
    const relativeX = $x - $myX;
    return `translateX(${relativeX}px)`;
  }};
  transition: transform ${TRANSITION_DURATION_MS}ms ease-in-out;
  z-index: 10;
`;

export const RotatingWrapper = styled.div`
  will-change: transform;
`;
