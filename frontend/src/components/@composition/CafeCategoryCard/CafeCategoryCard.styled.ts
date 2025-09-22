import styled from '@emotion/styled';
import { theme } from '@/styles/theme';
import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';

type Props = {
  $isTouching: boolean;
};

export const Container = styled.button<Props>`
  position: relative;
  cursor: pointer;
  background-color: ${({ theme }) => theme.color.white};
  width: 100%;

  ${({ theme, $isTouching }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[100],
      isTouching: $isTouching,
      enableScale: false,
    })}

  isolation: isolate;
`;

export const CategoryName = styled.span`
  ${theme.typography.paragraph}
  display: flex;
  align-items: start;
`;
