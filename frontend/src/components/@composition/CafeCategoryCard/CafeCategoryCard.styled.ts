import styled from '@emotion/styled';
import { theme } from '@/styles/theme';
import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import { TouchState } from '@/types/touchState';

type Props = {
  $touchState: TouchState;
};

export const Container = styled.button<Props>`
  position: relative;
  cursor: pointer;
  background-color: ${({ theme }) => theme.color.white};
  width: 100%;

  ${({ theme, $touchState }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[100],
      touchState: $touchState,
      enableScale: false,
    })}
`;

export const CategoryName = styled.span`
  ${theme.typography.paragraph}
  display: flex;
  align-items: start;
`;
