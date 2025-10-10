import { useTheme } from '@emotion/react';

import { usePressAnimation } from '@/hooks/usePressAnimation';

import CloseIcon from '@/components/@common/CloseIcon/CloseIcon';
import Headline3 from '@/components/@common/Headline3/Headline3';

import * as S from './ModalHeader.styled';

type Props = {
  id?: string;
  title?: string;
  onClose: () => void;
  showCloseButton?: boolean;
};

const ModalHeader = ({ id, title, onClose, showCloseButton = true }: Props) => {
  const theme = useTheme();
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container>
      <Headline3 id={id}>{title}</Headline3>
      {showCloseButton && (
        <S.CloseButton
          aria-label="close-icon"
          onPointerDown={onPointerDown}
          onPointerUp={(e) => {
            onPointerUp(e);
            onClose();
          }}
          $touchState={touchState}
        >
          <CloseIcon stroke={theme.color.gray[600]} />
        </S.CloseButton>
      )}
    </S.Container>
  );
};

export default ModalHeader;
