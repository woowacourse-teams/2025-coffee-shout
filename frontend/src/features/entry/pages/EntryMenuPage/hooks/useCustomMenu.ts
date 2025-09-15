import { useState } from 'react';

export const useCustomMenu = () => {
  const [customMenuName, setCustomMenuName] = useState<string | null>(null);
  const [isCustomMenuInputCompleted, setIsCustomMenuInputCompleted] = useState(false);

  const completeMenuInput = () => {
    setIsCustomMenuInputCompleted(true);
  };

  const resetCustomMenu = () => {
    setCustomMenuName(null);
    setIsCustomMenuInputCompleted(false);
  };

  return {
    customMenuName,
    isCustomMenuInputCompleted,
    setCustomMenuName,
    completeMenuInput,
    resetCustomMenu,
  };
};
