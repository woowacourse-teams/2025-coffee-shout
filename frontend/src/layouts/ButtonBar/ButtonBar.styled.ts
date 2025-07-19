import styled from '@emotion/styled';

type WrapperProps = {
  $flexRatio: number; // $width → $flexRatio로 변경
};

export const Container = styled.div`
  width: 100%;
  height: 4rem;
  display: flex;
  gap: 1.5rem;
`;

export const Wrapper = styled.div<WrapperProps>`
  flex: ${({ $flexRatio }) => $flexRatio};
`;
