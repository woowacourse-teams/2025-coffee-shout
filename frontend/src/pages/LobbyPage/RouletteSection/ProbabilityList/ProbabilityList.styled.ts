import styled from '@emotion/styled';
import Headline4 from '@/components/@common/Headline4/Headline4';

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

export const ProbabilityText = styled(Headline4)`
  color: #666;
`;
