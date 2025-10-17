import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import { TouchState } from '@/types/touchState';
import styled from '@emotion/styled';

type Props = {
  $touchState: TouchState;
};

export const Container = styled.button<Props>`
  display: flex;
  align-items: center;
  width: 100%;
  height: 50px;
  border-bottom: 1px solid ${({ theme }) => theme.color.gray[200]};
  padding-left: 16px;
  background: none;
  cursor: pointer;

  ${({ theme, $touchState }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[100],
      touchState: $touchState,
      enableScale: false,
    })}
`;
