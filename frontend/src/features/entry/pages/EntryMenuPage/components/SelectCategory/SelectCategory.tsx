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
        categories.map((category, index) => {
          const isLast = index === categories.length - 1;
          return (
            <CafeCategoryCard
              key={category.id}
              imageUrl={category.imageUrl}
              categoryName={category.name}
              onClick={() => onClickCategory(category)}
              color={categoryColorList[index % categoryColorList.length]}
              position={index + 1}
              totalCount={categories.length}
              ariaLabel={`${category.name} 선택, ${index + 1}번째 카테고리${isLast ? ', 마지막 카테고리' : ''}`}
            />
          );
        })
      )}
    </S.CategoryList>
  );
};

export default SelectCategory;
