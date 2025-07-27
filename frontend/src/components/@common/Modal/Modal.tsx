import Portal from '@/components/@common/Portal/Portal';
import useEscapeKey from '@/hooks/useEscapeKey';
import useFocusTrap from '@/hooks/useFocusTrap';
import { MouseEvent, PropsWithChildren, useId } from 'react';
import * as S from './Modal.styled';
import ModalHeader from './ModalHeader/ModalHeader';

type Props = {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  showCloseButton?: boolean;
} & PropsWithChildren;

const Modal = ({ isOpen, onClose, children, title, showCloseButton = true }: Props) => {
  useEscapeKey({ onEscape: onClose, enabled: isOpen });

  const id = useId();
  const titleId = `modal-title-${id}`;
  const contentId = `modal-content-${id}`;

  const { containerRef } = useFocusTrap(isOpen);

  const stopPropagation = (e: MouseEvent<HTMLDivElement>) => e.stopPropagation();

  if (!isOpen) return null;

  const shouldRenderHeader = title || showCloseButton;

  return (
    <Portal containerId="modal-root">
      <S.Backdrop onClick={onClose} role="presentation">
        <S.Container
          ref={containerRef}
          onClick={stopPropagation}
          role="dialog"
          aria-modal="true"
          aria-labelledby={title ? titleId : undefined}
          aria-describedby={contentId}
        >
          {shouldRenderHeader && (
            <ModalHeader
              id={titleId}
              title={title}
              onClose={onClose}
              showCloseButton={showCloseButton}
            />
          )}
          <div id={contentId}>{children}</div>
        </S.Container>
      </S.Backdrop>
    </Portal>
  );
};

export default Modal;
