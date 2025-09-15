import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

type CurrentView = 'selectCategory' | 'selectMenu' | 'inputCustomMenu' | 'selectTemperature';

export const useViewNavigation = () => {
  const navigate = useNavigate();
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

  const handleNavigateToBefore = (onResetMenu: () => void, onResetCustomMenu: () => void) => {
    switch (currentView) {
      case 'selectCategory':
        navigateToName();
        break;
      case 'selectMenu':
        navigateToCategory();
        break;
      case 'inputCustomMenu':
        onResetCustomMenu();
        navigateToCategory();
        break;
      case 'selectTemperature':
        onResetMenu();
        onResetCustomMenu();
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
