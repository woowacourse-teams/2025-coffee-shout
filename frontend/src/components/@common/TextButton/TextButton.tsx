import { type ComponentProps } from 'react';

import { usePressAnimation } from '@/hooks/usePressAnimation';

import * as S from './TextButton.styled';

type Props = {
  text: string;
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const TextButton = ({ text, onClick, ...rest }: Props) => {
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container
      onPointerDown={onPointerDown}
      onPointerUp={(e) => {
        onPointerUp(e);
        onClick();
      }}
      $touchState={touchState}
      {...rest}
    >
      {text}
    </S.Container>
  );
};

export default TextButton;
