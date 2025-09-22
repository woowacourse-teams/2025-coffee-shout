import BackIcon from '@/assets/back-icon.svg';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import { ComponentProps, MouseEvent, TouchEvent } from 'react';
import * as S from './BackButton.styled';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';

type Props = {
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
} & ComponentProps<'button'>;

const BackButton = ({ onClick, ...rest }: Props) => {
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
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      $isTouching={isTouching}
      {...rest}
    >
      <img src={BackIcon} alt="뒤로가기" />
    </S.Container>
  );
};

export default BackButton;
