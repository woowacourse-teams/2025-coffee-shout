import * as S from './SelectCategory.styled';
import CafeCategoryCard from '@/components/@composition/CafeCategoryCard/CafeCategoryCard';
import CafeCategoryCardSkeleton from '@/components/@composition/CafeCategoryCardSkeleton/CafeCategoryCardSkeleton';
import { CategoryWithColor } from '@/types/menu';
import { categoryColorList } from '@/constants/color';
import { useCategories } from '../../hooks/useCategories';

type Props = {
  onClickCategory: (category: CategoryWithColor) => void;
};

const SelectCategory = ({ onClickCategory }: Props) => {
  const { categories, loading } = useCategories();

  return (
    <S.CategoryList>
      {loading ? (
        <CafeCategoryCardSkeleton />
      ) : (
        categories.map((category, index) => (
          <CafeCategoryCard
            key={category.id}
            imageUrl={category.imageUrl}
            categoryName={category.name}
            onClick={() => onClickCategory(category)}
            color={categoryColorList[index % categoryColorList.length]}
          />
        ))
      )}
    </S.CategoryList>
  );
};

export default SelectCategory;
