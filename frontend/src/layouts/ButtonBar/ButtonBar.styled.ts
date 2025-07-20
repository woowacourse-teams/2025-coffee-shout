import styled from '@emotion/styled';

type WrapperProps = {
  $flexRatio: number;
};

type ContainerProps = {
  $height: string;
};

export const Container = styled.div<ContainerProps>`
  width: 100%;
  height: ${({ $height }) => $height};
  display: flex;
  gap: 1.5rem;
`;

export const Wrapper = styled.div<WrapperProps>`
  flex: ${({ $flexRatio }) => $flexRatio};
`;
