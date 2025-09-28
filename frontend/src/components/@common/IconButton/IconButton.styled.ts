import { buttonHoverPress } from '@/styles/animations/buttonHoverPress';
import styled from '@emotion/styled';

type Props = {
  $touchState: 'idle' | 'pressing' | 'releasing';
};

export const Container = styled.button<Props>`
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: ${({ theme }) => theme.color.gray[100]};

  ${({ theme, $touchState }) =>
    buttonHoverPress({
      activeColor: theme.color.gray[200],
      touchState: $touchState,
    })}
`;

export const Icon = styled.img`
  width: 25px;
  height: 25px;
  opacity: 0.5;
`;
