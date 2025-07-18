import { LAYOUT_PADDING } from '@/constants/padding';
import styled from '@emotion/styled';

type Props = {
  $color?: string;
};

export const LayoutContainer = styled.div<Props>`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: ${LAYOUT_PADDING};
  background-color: ${({ $color }) => $color};
`;
