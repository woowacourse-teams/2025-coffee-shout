import { useState, useEffect } from 'react';
import { Menu } from '@/types/menu';
import { api } from '@/apis/rest/api';

export const useMenu = (selectedCategoryId: number | null) => {
  const [menus, setMenus] = useState<Menu[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!selectedCategoryId) {
      setMenus([]);
      return;
    }

    const fetchMenus = async () => {
      try {
        setLoading(true);
        setError(null);
        const menus = await api.get<Menu[]>(`/menu-categories/${selectedCategoryId}/menus`);
        setMenus(menus);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('메뉴를 불러오는데 실패했습니다'));
        setMenus([]);
      } finally {
        setLoading(false);
      }
    };

    fetchMenus();
  }, [selectedCategoryId]);

  const resetMenus = () => {
    setMenus([]);
  };

  return { menus, loading, error, resetMenus };
};
