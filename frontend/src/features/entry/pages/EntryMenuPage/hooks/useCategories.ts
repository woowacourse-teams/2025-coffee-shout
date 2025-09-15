import { useState, useEffect } from 'react';
import { api } from '@/apis/rest/api';
import { Category, CategoryWithColor } from '@/types/menu';
import { categoryColorList } from '@/constants/color';

type CategoriesResponse = Category[];

export const useCategories = () => {
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState<CategoryWithColor[]>([]);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await api.get<CategoriesResponse>('/menu-categories');
        setCategories(
          data.map((category, index) => ({
            ...category,
            color: categoryColorList[index % categoryColorList.length],
          }))
        );
      } catch (err) {
        setError(err instanceof Error ? err : new Error('카테고리를 불러오는데 실패했습니다'));
        setCategories([]);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  return {
    loading,
    categories,
    error,
  };
};
