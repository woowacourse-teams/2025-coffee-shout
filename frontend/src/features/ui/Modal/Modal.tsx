import Portal from '@/components/@common/Portal/Portal';
import useEscapeKey from '@/hooks/useEscapeKey';
import { MouseEvent, PropsWithChildren } from 'react';
import * as S from './Modal.styled';
import ModalHeader from './ModalHeader';

type Props = {
  isOpen: boolean;
  onClose: () => void;
} & PropsWithChildren;

const Modal = ({ isOpen, onClose, children }: Props) => {
  useEscapeKey({ onEscape: onClose, enabled: isOpen });
  const stopPropagation = (e: MouseEvent<HTMLDivElement>) => e.stopPropagation();

  if (!isOpen) return null;

  return (
    <Portal containerId="modal-root">
      <S.Backdrop onClick={onClose}>
        <S.Container onClick={stopPropagation}>{children}</S.Container>
      </S.Backdrop>
    </Portal>
  );
};

Modal.Header = ModalHeader;

export default Modal;
