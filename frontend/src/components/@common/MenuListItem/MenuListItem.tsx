import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import Paragraph from '../Paragraph/Paragraph';
import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction();

  return (
    <S.Container
      onPointerUp={onClick}
      $touchState={touchState}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
