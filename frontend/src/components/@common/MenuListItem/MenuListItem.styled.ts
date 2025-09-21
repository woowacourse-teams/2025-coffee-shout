import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import styled from '@emotion/styled';

interface Props {
  $isTouching: boolean;
}

export const Container = styled.button<Props>`
  display: flex;
  align-items: center;
  width: 100%;
  height: 50px;
  border-bottom: 1px solid ${({ theme }) => theme.color.gray[200]};
  padding-left: 16px;
  background: none;
  cursor: pointer;

  ${({ theme, $isTouching }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[50],
      isTouching: $isTouching,
      enableScale: false,
    })}
`;
