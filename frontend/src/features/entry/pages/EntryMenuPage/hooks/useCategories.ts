import { useMemo } from 'react';
import { Category, CategoryWithColor } from '@/types/menu';
import { categoryColorList } from '@/constants/color';
import useFetch from '@/apis/rest/useFetch';

type CategoriesResponse = Category[];

export const useCategories = () => {
  const {
    data: categoriesData,
    loading,
    error,
  } = useFetch<CategoriesResponse>({
    endpoint: '/menu-categories',
  });

  const categories: CategoryWithColor[] = useMemo(() => {
    if (!categoriesData) return [];
    return categoriesData.map((category, index) => ({
      ...category,
      color: categoryColorList[index % categoryColorList.length],
    }));
  }, [categoriesData]);

  return {
    loading,
    categories,
    error,
  };
};
