import { useEffect } from 'react';
import { useBlocker } from 'react-router-dom';

type Props = {
  onConfirm?: () => void;
  message?: string;
};

export const useBackButtonConfirm = ({
  onConfirm,
  message = '정말 페이지를 나가시겠습니까?',
}: Props = {}) => {
  const blocker = useBlocker(({ historyAction }) => historyAction === 'POP');

  useEffect(() => {
    if (blocker.state === 'blocked') {
      const confirmed = window.confirm(message);
      if (confirmed) {
        blocker.proceed();
        if (onConfirm) onConfirm();
      } else {
        blocker.reset();
      }
    }
  }, [blocker, onConfirm, message]);
};
