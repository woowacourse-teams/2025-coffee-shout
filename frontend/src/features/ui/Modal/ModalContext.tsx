import { createContext, PropsWithChildren, ReactNode, useState } from 'react';
import Modal from './Modal';

type Options = {
  title?: string;
  showCloseButton?: boolean;
};

type ModalContextType = {
  openModal: (content: ReactNode, options?: Options) => void;
  closeModal: () => void;
};

export const ModalContext = createContext<ModalContextType | null>(null);

export const ModalProvider = ({ children }: PropsWithChildren) => {
  const [content, setContent] = useState<ReactNode | null>(null);
  const [options, setOptions] = useState<Options>({});

  const openModal = (content: ReactNode, options: Options = {}) => {
    setContent(content);
    setOptions(options);
  };

  const closeModal = () => {
    setContent(null);
    setOptions({});
  };

  return (
    <ModalContext.Provider value={{ openModal, closeModal }}>
      {children}
      <Modal
        isOpen={content !== null}
        onClose={closeModal}
        title={options.title}
        showCloseButton={options.showCloseButton}
      >
        {content}
      </Modal>
    </ModalContext.Provider>
  );
};
