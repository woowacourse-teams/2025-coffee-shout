import { useState, useEffect } from 'react';
import { api } from '@/apis/rest/api';
import { Category, CategoryWithColor } from '@/types/menu';
import { categoryColorList } from '@/constants/color';

type CategoriesResponse = Category[];

export const useCategories = () => {
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState<CategoryWithColor[]>([]);

  useEffect(() => {
    (async () => {
      const data = await api.get<CategoriesResponse>('/menu-categories');
      setCategories(
        data.map((category, index) => ({
          ...category,
          color: categoryColorList[index % categoryColorList.length],
        }))
      );
      setLoading(false);
    })();
  }, []);

  return {
    loading,
    categories,
  };
};
