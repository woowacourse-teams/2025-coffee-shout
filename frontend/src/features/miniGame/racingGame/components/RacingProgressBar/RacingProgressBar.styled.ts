import styled from '@emotion/styled';

const CONTAINER_PADDING = '1rem 2rem';
const CONTAINER_PADDING_TOP = '3rem';
const TRACK_HEIGHT = '20px';
const TRACK_BORDER_RADIUS = '10px';
const TRACK_BACKGROUND_OPACITY = 0.3;

const FILL_OPACITY_ME = 1;
const FILL_OPACITY_OTHER = 0.6;
const FILL_TRANSITION_DURATION = '0.3s';
const FILL_BORDER_RADIUS_THRESHOLD = 99;

const MARKER_SIZE_ME = '24px';
const MARKER_SIZE_OTHER = '20px';
const MARKER_BORDER_WIDTH = '2px';
const MARKER_BORDER_COLOR_ME = '#fff';
const MARKER_TOP_OFFSET = '-20px';
const MARKER_SHADOW = '0 2px 8px rgba(0, 0, 0, 0.3)';
const MARKER_AFTER_HEIGHT_ME = '10px';
const MARKER_AFTER_HEIGHT_OTHER = '8px';
const MARKER_AFTER_BOTTOM_ME = '-10px';
const MARKER_AFTER_BOTTOM_OTHER = '-8px';
const MARKER_AFTER_WIDTH = '2px';

export const Container = styled.div`
  width: 100%;
  padding: ${CONTAINER_PADDING};
  position: relative;
  padding-top: ${CONTAINER_PADDING_TOP};
`;

export const ProgressTrack = styled.div`
  position: relative;
  width: 100%;
  height: ${TRACK_HEIGHT};
  background-color: rgba(255, 255, 255, ${TRACK_BACKGROUND_OPACITY});
  border-radius: ${TRACK_BORDER_RADIUS};
  overflow: visible;
`;

export const ProgressFill = styled.div<{ $progress: number; $color: string; $isMe: boolean }>`
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: ${({ $progress }) => $progress}%;
  background-color: ${({ $color }) => $color};
  opacity: ${({ $isMe }) => ($isMe ? FILL_OPACITY_ME : FILL_OPACITY_OTHER)};
  border-radius: ${({ $progress }) =>
    $progress >= FILL_BORDER_RADIUS_THRESHOLD
      ? TRACK_BORDER_RADIUS
      : `${TRACK_BORDER_RADIUS} 0 0 ${TRACK_BORDER_RADIUS}`};
  transition:
    width ${FILL_TRANSITION_DURATION} ease-out,
    border-radius ${FILL_TRANSITION_DURATION} ease-out;
  z-index: ${({ $isMe }) => ($isMe ? 2 : 1)};
`;

export const ProgressMarker = styled.div<{ $progress: number; $color: string; $isMe: boolean }>`
  position: absolute;
  left: ${({ $progress }) => $progress}%;
  top: ${MARKER_TOP_OFFSET};
  transform: translateX(-50%);
  width: ${({ $isMe }) => ($isMe ? MARKER_SIZE_ME : MARKER_SIZE_OTHER)};
  height: ${({ $isMe }) => ($isMe ? MARKER_SIZE_ME : MARKER_SIZE_OTHER)};
  background-color: ${({ $color }) => $color};
  border: ${MARKER_BORDER_WIDTH} solid
    ${({ $isMe }) => ($isMe ? MARKER_BORDER_COLOR_ME : 'transparent')};
  border-radius: 50%;
  box-shadow: ${MARKER_SHADOW};
  transition: left ${FILL_TRANSITION_DURATION} ease-out;
  z-index: ${({ $isMe }) => ($isMe ? 3 : 2)};

  &::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: ${({ $isMe }) => ($isMe ? MARKER_AFTER_BOTTOM_ME : MARKER_AFTER_BOTTOM_OTHER)};
    transform: translateX(-50%);
    width: ${MARKER_AFTER_WIDTH};
    height: ${({ $isMe }) => ($isMe ? MARKER_AFTER_HEIGHT_ME : MARKER_AFTER_HEIGHT_OTHER)};
    background-color: ${({ $color }) => $color};
  }
`;
