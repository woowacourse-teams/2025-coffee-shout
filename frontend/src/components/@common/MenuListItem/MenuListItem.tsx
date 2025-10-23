import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import Paragraph from '../Paragraph/Paragraph';

import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container {...pointerHandlers} $touchState={touchState}>
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
