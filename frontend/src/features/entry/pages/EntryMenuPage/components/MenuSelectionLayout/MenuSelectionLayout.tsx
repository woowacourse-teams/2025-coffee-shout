import { PropsWithChildren } from 'react';
import * as S from './MenuSelectionLayout.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import Headline3 from '@/components/@common/Headline3/Headline3';

type Props = {
  categorySelection: CategorySelection;
  menuSelection: MenuSelection;
  showSelectedMenuCard?: boolean;
} & PropsWithChildren;

type CategorySelection = {
  color: string;
  name: string;
  imageUrl: string;
};

type MenuSelection = {
  color: string;
  name: string;
};

const MenuSelectionLayout = ({
  categorySelection,
  showSelectedMenuCard = false,
  menuSelection,
  children,
}: Props) => {
  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard
          color={categorySelection.color}
          text={categorySelection.name}
          imageUrl={categorySelection.imageUrl}
        />
        {showSelectedMenuCard && (
          <SelectionCard color={menuSelection.color} text={menuSelection.name} />
        )}
        {children}
      </S.Wrapper>
    </>
  );
};

export default MenuSelectionLayout;
