import { Menu } from '@/types/menu';
import useFetch from '@/apis/rest/useFetch';

type MenusResponse = Menu[];

export const useMenus = (selectedCategoryId: number | null) => {
  const {
    data: menusData,
    loading,
    error,
  } = useFetch<MenusResponse>({
    endpoint: `/menu-categories/${selectedCategoryId}/menus`,
    enabled: !!selectedCategoryId,
  });

  const menus = menusData ?? [];

  return { menus, loading, error };
};
