import styled from '@emotion/styled';
import { LAYOUT_PADDING } from '@/constants/padding';
import skyImage from '@/assets/sky.png';

export const Container = styled.div<{ $speed: number }>`
  width: 100%;
  height: 100%;
  padding: ${LAYOUT_PADDING} 0;
  background-image: url(${skyImage});
  background-size: cover;
  background-position: center;
  background-repeat: repeat-x;
  display: flex;
  flex-direction: column;
  animation: moveBackground ${({ $speed }) => 40 / $speed}s linear infinite;

  @keyframes moveBackground {
    from {
      background-position: 0% center;
    }
    to {
      background-position: 100% center;
    }
  }
`;

export const HeadlineWrapper = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 2.6rem;
`;

export const ContentWrapper = styled.div`
  width: 100%;

  flex: 1;
  overflow: hidden;
`;

export const PlayersWrapper = styled.div`
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 3rem;
  justify-content: center;
  align-items: center;
`;
