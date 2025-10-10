import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import * as S from './CustomMenuButton.styled';
import WriteIcon from '@/assets/write-icon.svg';
import { MouseEvent, TouchEvent } from 'react';

type Props = {
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
};

const CustomMenuButton = ({ onClick }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({ onClick });

  return (
    <S.Container
      onClick={onClick}
      $touchState={touchState}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
