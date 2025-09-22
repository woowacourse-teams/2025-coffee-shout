import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import * as S from './CustomMenuButton.styled';
import WriteIcon from '@/assets/write-icon.svg';
import { MouseEvent, TouchEvent } from 'react';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';

interface Props {
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();
  const isTouchDevice = checkIsTouchDevice();

  const handleTouchStart = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;

    e.preventDefault();
    startTouchPress();
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;

    e.preventDefault();
    endTouchPress(onClick, e);
  };

  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
