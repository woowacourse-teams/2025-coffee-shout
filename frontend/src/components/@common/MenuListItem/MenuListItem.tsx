import { usePressAnimation } from '@/hooks/usePressAnimation';

import Paragraph from '../Paragraph/Paragraph';

import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container
      onPointerDown={onPointerDown}
      onPointerUp={(e) => {
        onPointerUp(e);
        onClick();
      }}
      $touchState={touchState}
    >
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
