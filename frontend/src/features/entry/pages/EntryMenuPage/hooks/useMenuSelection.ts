import { useState } from 'react';
import { CategoryWithColor, Menu, TemperatureOption } from '@/types/menu';

export const useMenuSelection = () => {
  const [selectedCategory, setSelectedCategory] = useState<CategoryWithColor | null>(null);
  const [selectedMenu, setSelectedMenu] = useState<Menu | null>(null);
  const [selectedTemperature, setSelectedTemperature] = useState<TemperatureOption>('ICE');

  const selectCategory = (category: CategoryWithColor) => {
    setSelectedCategory(category);
  };

  const selectMenu = (menu: Menu) => {
    setSelectedMenu(menu);
    if (menu.temperatureAvailability === 'ICE_ONLY') {
      setSelectedTemperature('ICE');
    } else if (menu.temperatureAvailability === 'HOT_ONLY') {
      setSelectedTemperature('HOT');
    }
  };

  const selectTemperature = (temperature: TemperatureOption) => {
    setSelectedTemperature(temperature);
  };

  const resetMenuSelection = () => {
    setSelectedCategory(null);
    setSelectedMenu(null);
    setSelectedTemperature('ICE');
  };

  return {
    selectedCategory,
    selectedMenu,
    selectedTemperature,
    selectCategory,
    selectMenu,
    selectTemperature,
    resetMenuSelection,
  };
};
