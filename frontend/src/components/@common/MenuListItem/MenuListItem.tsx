import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import Paragraph from '../Paragraph/Paragraph';

import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
  ariaLabel?: string;
};

const MenuListItem = ({ text, onClick, ariaLabel, ...rest }: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container {...pointerHandlers} $touchState={touchState} aria-label={ariaLabel} {...rest}>
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
