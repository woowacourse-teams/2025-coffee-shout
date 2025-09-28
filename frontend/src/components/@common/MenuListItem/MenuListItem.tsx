import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import Paragraph from '../Paragraph/Paragraph';
import * as S from './MenuListItem.styled';
import { MouseEvent, TouchEvent } from 'react';

type Props = {
  text: string;
  onClick: (e: MouseEvent<HTMLButtonElement> | TouchEvent<HTMLButtonElement>) => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({ onClick });

  return (
    <S.Container
      onClick={onClick}
      $touchState={touchState}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
