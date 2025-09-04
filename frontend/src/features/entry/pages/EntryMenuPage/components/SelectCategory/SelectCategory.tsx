import * as S from './SelectCategory.styled';
import CafeCategoryCard from '@/components/@composition/CafeCategoryCard/CafeCategoryCard';
import Headline3 from '@/components/@common/Headline3/Headline3';
import CoffeeIcon from '@/assets/coffee.svg';

const SelectCategory = () => {
  return (
    <>
      <Headline3>카테고리를 선택해주세요</Headline3>
      <S.CategoryList>
        <CafeCategoryCard iconSrc={CoffeeIcon} categoryName="커피" onClick={() => {}} />
        <CafeCategoryCard iconSrc={CoffeeIcon} categoryName="스무디" onClick={() => {}} />
        <CafeCategoryCard iconSrc={CoffeeIcon} categoryName="티" onClick={() => {}} />
        <CafeCategoryCard iconSrc={CoffeeIcon} categoryName="에이드" onClick={() => {}} />
      </S.CategoryList>
    </>
  );
};

export default SelectCategory;
