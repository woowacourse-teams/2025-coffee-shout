import { PropsWithChildren } from 'react';
import * as S from './MenuSelectionLayout.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';

type Props = {
  title: string;
  categorySelectionCard: CategorySelectionCard;
  menuSelectionCard: MenuSelectionCard;
  showSelectedMenuCard?: boolean;
  showChildren?: boolean;
} & PropsWithChildren;

type CategorySelectionCard = {
  color: string;
  text: string;
  imageUrl: string;
};

type MenuSelectionCard = {
  color: string;
  text: string;
};

const MenuSelectionLayout = ({
  categorySelectionCard,
  showSelectedMenuCard = false,
  menuSelectionCard,
  showChildren = false,
  children,
}: Props) => {
  return (
    <>
      <S.Wrapper>
        <SelectionCard
          color={categorySelectionCard.color}
          text={categorySelectionCard.text}
          imageUrl={categorySelectionCard.imageUrl}
        />
        {showSelectedMenuCard && (
          <SelectionCard color={menuSelectionCard.color} text={menuSelectionCard.text} />
        )}
        {showChildren && children}
      </S.Wrapper>
    </>
  );
};

export default MenuSelectionLayout;
