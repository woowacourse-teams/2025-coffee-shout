import { LAYOUT_PADDING } from '@/constants/padding';
import styled from '@emotion/styled';

type Props = {
  $height?: string;
};

export const Container = styled.div<Props>`
  width: calc(100% + 2rem);
  height: ${({ $height }) => $height};
  background-color: ${({ theme }) => theme.color.point[400]};

  margin-top: -${LAYOUT_PADDING};
  margin-left: -${LAYOUT_PADDING};
  margin-right: -${LAYOUT_PADDING};
  margin-bottom: ${LAYOUT_PADDING};

  border-radius: 0 0 12px 12px;
`;
