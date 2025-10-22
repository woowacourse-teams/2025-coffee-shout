import { useEffect } from 'react';
import { useBlocker } from 'react-router-dom';

export const useBackButtonConfirm = () => {
  const blocker = useBlocker(({ historyAction }) => historyAction === 'POP');

  useEffect(() => {
    if (blocker.state === 'blocked') {
      const confirmed = window.confirm('정말 페이지를 나가시겠습니까?');
      if (confirmed) {
        blocker.proceed();
      } else {
        blocker.reset();
      }
    }
  }, [blocker]);
};
