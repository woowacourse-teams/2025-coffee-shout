import Portal from '@/components/@common/Portal/Portal';
import { MouseEvent, PropsWithChildren } from 'react';
import * as S from './Modal.styled';

type Props = {
  open: boolean;
  onClose: () => void;
} & PropsWithChildren;

const Modal = ({ open, onClose, children }: Props) => {
  const stopPropagation = (e: MouseEvent<HTMLDivElement>) => e.stopPropagation();

  if (!open) return null;

  return (
    <Portal containerId="modal-root">
      <S.Backdrop onClick={onClose}>
        <S.Container onClick={stopPropagation}>
          <S.Content>{children}</S.Content>
        </S.Container>
      </S.Backdrop>
    </Portal>
  );
};

export default Modal;
