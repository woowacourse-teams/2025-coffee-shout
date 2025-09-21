import styled from '@emotion/styled';
import { theme } from '@/styles/theme';
import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';

type Props = {
  $isTouching: boolean;
};

export const CategoryName = styled.span`
  ${theme.typography.paragraph}
  display: flex;
  align-items: start;
`;

export const Container = styled.button<Props>`
  cursor: pointer;
  border: none;
  background: none;
  width: 100%;

  ${({ theme, $isTouching }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[50],
      isTouching: $isTouching,
      enableScale: false,
    })}
`;
