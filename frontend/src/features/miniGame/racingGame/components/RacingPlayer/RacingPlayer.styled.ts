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

export const PlayerName = styled.div`
  position: absolute;
  top: -1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  width: 100%;
`;
