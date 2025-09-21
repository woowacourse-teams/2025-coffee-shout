import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import Paragraph from '../Paragraph/Paragraph';
import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();

  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
      onTouchStart={startTouchPress}
      onTouchEnd={endTouchPress}
    >
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
