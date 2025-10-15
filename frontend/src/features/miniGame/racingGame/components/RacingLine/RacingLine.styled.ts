import styled from '@emotion/styled';
import { RACING_Z_INDEX } from '../../constants/zIndex';

const TRANSITION_DURATION = 300;

type Props = {
  $position: number;
};

export const Container = styled.div<Props>`
  position: absolute;
  left: 50%;
  top: 0;
  width: 30px;
  height: 100%;
  transform: translateX(${({ $position }) => $position}px);
  transition: transform ${TRANSITION_DURATION}ms ease-in-out;
  z-index: ${RACING_Z_INDEX.LINE};
`;

export const Image = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;
