import type { ComponentProps } from 'react';

import BackIcon from '@/assets/back-icon.svg';
import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import * as S from './BackButton.styled';

type Props = {
  onClick: () => void;
} & ComponentProps<'button'>;

const BackButton = ({ onClick, ...rest }: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container {...pointerHandlers} $touchState={touchState} {...rest}>
      <img src={BackIcon} alt="뒤로가기" />
    </S.Container>
  );
};

export default BackButton;
