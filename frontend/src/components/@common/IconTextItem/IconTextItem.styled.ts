import styled from '@emotion/styled';

type WrapperProps = {
  gap: number;
};

export const Container = styled.div`
  display: flex;
  align-items: center;
  width: 100%;
  padding: 15px 0;
  justify-content: space-between;
`;

export const Wrapper = styled.div<WrapperProps>`
  display: flex;
  align-items: center;
  gap: ${({ gap }) => gap}px;
  flex: 1;
  min-width: 0;
`;

export const IconWrapper = styled.div`
  flex-shrink: 0;
`;

export const TextWrapper = styled.div`
  flex: 1;
  min-width: 0;
  padding-right: 20px;

  h1,
  h2,
  h3,
  h4,
  h5,
  h6,
  p,
  span,
  div {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
`;
