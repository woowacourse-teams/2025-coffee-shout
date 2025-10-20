import { PropsWithChildren, useEffect, useRef } from 'react';
import * as S from './MenuSelectionLayout.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import Headline3 from '@/components/@common/Headline3/Headline3';
import ScreenReaderOnly from '@/components/@common/ScreenReaderOnly/ScreenReaderOnly';

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
  const liveRef = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (liveRef.current) {
      liveRef.current.focus();
    }
  }, []);

  return (
    <>
      <Headline3 ref={liveRef} tabIndex={0}>
        메뉴를 선택해주세요
      </Headline3>
      <ScreenReaderOnly>{`${categorySelection.name} 카테고리`}</ScreenReaderOnly>
      <S.Wrapper>
        <SelectionCard
          color={categorySelection.color}
          text={categorySelection.name}
          imageUrl={categorySelection.imageUrl}
        />
        {showSelectedMenuCard && (
          <SelectionCard color={menuSelection.color} text={menuSelection.name} />
        )}
        <S.ChildrenWrapper>{children}</S.ChildrenWrapper>
      </S.Wrapper>
    </>
  );
};

export default MenuSelectionLayout;
