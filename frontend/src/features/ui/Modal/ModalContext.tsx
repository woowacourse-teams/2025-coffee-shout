import { createContext, PropsWithChildren, ReactNode, useState } from 'react';
import Modal from './Modal';

type Props = PropsWithChildren;

type Options = {
  title?: string;
  showCloseButton?: boolean;
  hasHeader?: boolean;
};

type ModalContextType = {
  openModal: (content: ReactNode, options?: Options) => void;
  closeModal: () => void;
};

export const ModalContext = createContext<ModalContextType | null>(null);

export const ModalProvider = ({ children }: Props) => {
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

  const rendercontent = () => {
    const { title, showCloseButton = true, hasHeader = true } = options;

    const shouldRenderHeader = hasHeader && (title || showCloseButton);

    if (shouldRenderHeader) {
      return (
        <>
          <Modal.Header title={title} onClose={closeModal} showCloseButton={showCloseButton} />
          {content}
        </>
      );
    }

    return content;
  };

  return (
    <ModalContext.Provider value={{ openModal, closeModal }}>
      {children}
      <Modal isOpen={content !== null} onClose={closeModal}>
        {rendercontent()}
      </Modal>
    </ModalContext.Provider>
  );
};
