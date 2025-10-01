import styled from '@emotion/styled';

export const Container = styled.div<{ $x: number }>`
  position: absolute;
  left: 50%;
  top: 0;
  width: 30px;
  height: 100%;
  transform: translateX(${({ $x }) => $x}px);
  transition: transform 0.3s ease-in-out;
  z-index: 1;
`;

export const Image = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;
