import * as S from './SelectCategory.styled';
import CafeCategoryCard from '@/components/@composition/CafeCategoryCard/CafeCategoryCard';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { api } from '@/apis/rest/api';
import { Category } from '@/types/menu';

type Props = {
  categories: Category[];
  onClickCategory: (category: Category) => void;
};

const SelectCategory = ({ categories, onClickCategory }: Props) => {
  return (
    <>
      <Headline3>카테고리를 선택해주세요</Headline3>
      <S.CategoryList>
        {categories.map((category) => (
          <CafeCategoryCard
            key={category.id}
            imgUrl={category.imgUrl}
            categoryName={category.name}
            onClick={() => onClickCategory(category)}
          />
        ))}
      </S.CategoryList>
    </>
  );
};

export default SelectCategory;
