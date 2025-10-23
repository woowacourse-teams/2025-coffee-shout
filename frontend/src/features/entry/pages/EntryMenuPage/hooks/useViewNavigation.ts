import { useState } from 'react';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';

type CurrentView = 'selectCategory' | 'selectMenu' | 'inputCustomMenu' | 'selectTemperature';

export const useViewNavigation = () => {
  const navigate = useReplaceNavigate();
  const [currentView, setCurrentView] = useState<CurrentView>('selectCategory');

  const navigateToCategory = () => {
    setCurrentView('selectCategory');
  };

  const navigateToMenu = () => {
    setCurrentView('selectMenu');
  };

  const navigateToCustomMenu = () => {
    setCurrentView('inputCustomMenu');
  };

  const navigateToTemperature = () => {
    setCurrentView('selectTemperature');
  };

  const navigateToName = () => {
    navigate('/entry/name');
  };

  const handleNavigateToBefore = (onResetMenu: () => void) => {
    switch (currentView) {
      case 'selectCategory':
        navigateToName();
        break;
      case 'selectMenu':
        onResetMenu();
        navigateToCategory();
        break;
      case 'inputCustomMenu':
        onResetMenu();
        navigateToCategory();
        break;
      case 'selectTemperature':
        onResetMenu();
        navigateToCategory();
        break;
      default:
        navigateToName();
        break;
    }
  };

  return {
    currentView,
    setCurrentView,
    navigateToCategory,
    navigateToMenu,
    navigateToCustomMenu,
    navigateToTemperature,
    handleNavigateToBefore,
  };
};
