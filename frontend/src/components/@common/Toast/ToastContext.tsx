import { createContext, PropsWithChildren, useCallback, useEffect, useRef, useState } from 'react';
import Toast from './Toast';
import { ToastOptions, ToastType } from './types';

type ToastContextType = {
  showToast: (options: ToastOptions) => void;
};

export const ToastContext = createContext<ToastContextType | null>(null);

export const ToastProvider = ({ children }: PropsWithChildren) => {
  const [message, setMessage] = useState<string>('');
  const [type, setType] = useState<ToastType>('info');
  const [isExiting, setIsExiting] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const exitTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const clearTimer = useCallback(() => {
    if (timer.current) {
      clearTimeout(timer.current);
      timer.current = null;
    }
    if (exitTimer.current) {
      clearTimeout(exitTimer.current);
      exitTimer.current = null;
    }
  }, []);

  useEffect(() => {
    return () => clearTimer();
  }, [clearTimer]);

  const showToast = useCallback(
    ({ message, type = 'info', duration = 3000 }: ToastOptions) => {
      clearTimer();
      setIsExiting(false);

      setMessage(message);
      setType(type);

      timer.current = setTimeout(() => {
        setIsExiting(true);

        exitTimer.current = setTimeout(() => {
          setMessage('');
          setIsExiting(false);
          timer.current = null;
          exitTimer.current = null;
        }, 300);
      }, duration);
    },
    [clearTimer]
  );

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {!!message && <Toast message={message} type={type} isExiting={isExiting} />}
    </ToastContext.Provider>
  );
};
