import styled from '@emotion/styled';
import { RACING_Z_INDEX } from '../../constants/zIndex';

export const Container = styled.div<{ $position: number }>`
  position: absolute;
  left: 50%;
  top: 0;
  width: 30px;
  height: 100%;
  transform: translateX(${({ $position }) => $position}px);
  transition: transform 0.3s ease-in-out;
  z-index: ${RACING_Z_INDEX.LINE};
`;

export const Image = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;
