import type { ComponentProps } from 'react';

import BackIcon from '@/assets/back-icon.svg';
import { usePressAnimation } from '@/hooks/usePressAnimation';

import * as S from './BackButton.styled';

type Props = {
  onClick: () => void;
} & ComponentProps<'button'>;

const BackButton = ({ onClick, ...rest }: Props) => {
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
      <img src={BackIcon} alt="뒤로가기" />
    </S.Container>
  );
};

export default BackButton;
