import { useContext } from 'react';
import { ModalContext } from './ModalContext';

const useModal = () => {
  const context = useContext(ModalContext);
  if (!context) {
    throw new Error('useModal must be used within ModalProvider');
  }
  return context;
};

export default useModal;
