import styled from '@emotion/styled';

export const ScrollableWrapper = styled.div`
  overflow-y: auto;
  margin-bottom: 1.6rem;

  &::-webkit-scrollbar {
    display: none;
  }
`;

export const DividerWrapper = styled.div`
  padding: 0 16px;
`;

export const BottomGap = styled.div`
  height: 3rem;
`;
