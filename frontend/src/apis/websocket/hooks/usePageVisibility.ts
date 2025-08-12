import { useEffect, useState } from 'react';

export const usePageVisibility = () => {
  const [isVisible, setIsVisible] = useState<boolean>(!document.hidden);

  useEffect(() => {
    const handleVisibilityChange = () => {
      const visible = !document.hidden;
      setIsVisible(visible);

      if (visible) {
        console.log('📱 앱이 포그라운드로 전환됨');
      } else {
        console.log('📱 앱이 백그라운드로 전환됨');
      }
    };

    setIsVisible(!document.hidden);

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  return { isVisible };
};
