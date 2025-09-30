import { rippleEffect } from '@/styles/animations/effects/rippleEffect';
import { TouchState } from '@/types/touchState';
import styled from '@emotion/styled';

type Props = {
  $touchState: TouchState;
};

export const Container = styled.button<Props>`
  isolation: isolate;
  cursor: pointer;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: start;
  ${({ $touchState }) => rippleEffect($touchState)}
`;
