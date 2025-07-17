import Portal from '@/components/@common/Portal/Portal';
import useEscapeKey from '@/hooks/useEscapeKey';
import { MouseEvent, PropsWithChildren } from 'react';
import * as S from './Modal.styled';
import ModalHeader from './ModalHeader/ModalHeader';

type Props = {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  showCloseButton?: boolean;
  hasHeader?: boolean;
} & PropsWithChildren;

const Modal = ({
  isOpen,
  onClose,
  children,
  title,
  showCloseButton = true,
  hasHeader = true,
}: Props) => {
  useEscapeKey({ onEscape: onClose, enabled: isOpen });
  const stopPropagation = (e: MouseEvent<HTMLDivElement>) => e.stopPropagation();

  if (!isOpen) return null;

  const shouldRenderHeader = hasHeader && (title || showCloseButton);

  return (
    <Portal containerId="modal-root">
      <S.Backdrop onClick={onClose}>
        <S.Container onClick={stopPropagation}>
          {shouldRenderHeader && (
            <ModalHeader title={title} onClose={onClose} showCloseButton={showCloseButton} />
          )}
          {children}
        </S.Container>
      </S.Backdrop>
    </Portal>
  );
};

export default Modal;
