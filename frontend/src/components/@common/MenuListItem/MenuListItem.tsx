import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import Paragraph from '../Paragraph/Paragraph';
import * as S from './MenuListItem.styled';
import { MouseEvent, TouchEvent } from 'react';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';

type Props = {
  text: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
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
    endTouchPress(() => onClick(e));
  };

  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
