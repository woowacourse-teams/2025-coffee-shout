import styled from '@emotion/styled';

export const Container = styled.div`
  width: 100vw;
  height: 100vh;
  background-color: ${({ theme }) => theme.color.point[400]};
`;

export const Image = styled.img`
  width: 50%;
`;
