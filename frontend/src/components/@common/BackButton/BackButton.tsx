import BackIcon from '@/assets/back-icon.svg';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import { ComponentProps } from 'react';
import * as S from './BackButton.styled';

type Props = {
  onClick: () => void;
} & ComponentProps<'button'>;

const BackButton = ({ onClick, ...rest }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction();

  return (
    <S.Container
      onPointerUp={onClick}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      $touchState={touchState}
      {...rest}
    >
      <img src={BackIcon} alt="뒤로가기" />
    </S.Container>
  );
};

export default BackButton;
