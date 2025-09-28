import { useMenuState } from './useMenuState';
import { useCustomMenu } from './useCustomMenu';
import { MenuColorMap } from '@/constants/color';
import { theme } from '@/styles/theme';
import CustomMenuIcon from '@/assets/custom-menu-icon.svg';

export const useMenuFlow = () => {
  const {
    selectedCategory,
    selectedMenu,
    selectedTemperature,
    selectCategory,
    selectMenu,
    selectTemperature,
    resetMenuSelection,
  } = useMenuState();

  const { customMenuName, setCustomMenuName, completeMenuInput, resetCustomMenu } = useCustomMenu();

  const categorySelection = {
    color: selectedCategory?.color ?? theme.color.point[200],
    name: selectedCategory?.name ?? '직접입력',
    imageUrl: selectedCategory?.imageUrl ?? CustomMenuIcon,
  };

  const menuSelection = {
    color: MenuColorMap[selectedCategory?.color ?? theme.color.point[200]],
    name: selectedMenu?.name ?? customMenuName ?? '',
  };

  const temperatureAvailability = selectedMenu?.temperatureAvailability ?? 'BOTH';

  const resetAll = () => {
    resetMenuSelection();
    resetCustomMenu();
  };

  return {
    // 원시 상태들
    selectedCategory,
    selectedMenu,
    selectedTemperature,
    customMenuName,

    // 가공된 객체들
    categorySelection,
    menuSelection,
    temperatureAvailability,

    // 액션들
    selectCategory,
    selectMenu,
    selectTemperature,
    setCustomMenuName,
    completeMenuInput,
    resetAll,
  };
};
