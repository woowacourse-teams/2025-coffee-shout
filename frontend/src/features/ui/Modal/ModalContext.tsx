import { createContext, PropsWithChildren, ReactNode, useState } from 'react';
import Modal from './Modal';

interface ModalContextType {
  openModal: (modalContent: ReactNode) => void;
  closeModal: () => void;
}

export const ModalContext = createContext<ModalContextType | null>(null);

export const ModalProvider = ({ children }: PropsWithChildren) => {
  const [modalContent, setModalContent] = useState<ReactNode | null>(null);

  const openModal = (content: ReactNode) => setModalContent(content);
  const closeModal = () => setModalContent(null);

  return (
    <ModalContext.Provider value={{ openModal, closeModal }}>
      {children}
      <Modal open={modalContent !== null} onClose={closeModal}>
        {modalContent}
      </Modal>
    </ModalContext.Provider>
  );
};
