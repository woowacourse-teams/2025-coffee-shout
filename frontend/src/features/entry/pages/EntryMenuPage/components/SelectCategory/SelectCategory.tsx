import * as S from './SelectCategory.styled';
import CafeCategoryCard from '@/components/@composition/CafeCategoryCard/CafeCategoryCard';
import CafeCategoryCardSkeleton from '@/components/@composition/CafeCategoryCardSkeleton/CafeCategoryCardSkeleton';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { CategoryWithColor } from '@/types/menu';
import { categoryColorList } from '@/constants/color';

type Props = {
  categories: CategoryWithColor[];
  isCategoriesLoading: boolean;
  onClickCategory: (category: CategoryWithColor) => void;
};

const SelectCategory = ({ categories, isCategoriesLoading, onClickCategory }: Props) => {
  return (
    <>
      <Headline3>카테고리를 선택해주세요</Headline3>
      <S.CategoryList>
        {isCategoriesLoading
          ? Array.from({ length: 4 }).map((_, index) => <CafeCategoryCardSkeleton key={index} />)
          : categories.map((category, index) => (
              <CafeCategoryCard
                key={category.id}
                imageUrl={category.imageUrl}
                categoryName={category.name}
                onClick={() => onClickCategory(category)}
                color={categoryColorList[index % categoryColorList.length]}
              />
            ))}
      </S.CategoryList>
    </>
  );
};

export default SelectCategory;
