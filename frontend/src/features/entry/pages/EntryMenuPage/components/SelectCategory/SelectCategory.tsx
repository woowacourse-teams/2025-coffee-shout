import * as S from './SelectCategory.styled';
import CafeCategoryCard from '@/components/@composition/CafeCategoryCard/CafeCategoryCard';
import Headline3 from '@/components/@common/Headline3/Headline3';
import CoffeeIcon from '@/assets/coffee.svg';
import { useEffect, useState } from 'react';
import { api } from '@/apis/rest/api';
import { Category } from '@/types/menu';

type CategoriesResponse = Category[];
type Props = {
  onClickCategory: (category: Category) => void;
};

const SelectCategory = ({ onClickCategory }: Props) => {
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    (async () => {
      // const data = await api.get<CategoriesResponse>('/menu-categories');
      // setCategories(data);
      setCategories([
        {
          id: 1,
          name: '커피',
        },
        {
          id: 2,
          name: '스무디',
        },
        {
          id: 3,
          name: '에이드',
        },
        {
          id: 4,
          name: '티',
        },
        {
          id: 5,
          name: '티 라떼',
        },
      ]);
    })();
  }, []);

  return (
    <>
      <Headline3>카테고리를 선택해주세요</Headline3>
      <S.CategoryList>
        {categories.map((category) => (
          <CafeCategoryCard
            key={category.id}
            iconSrc={CoffeeIcon}
            categoryName={category.name}
            onClick={() => onClickCategory(category)}
          />
        ))}
      </S.CategoryList>
    </>
  );
};

export default SelectCategory;
