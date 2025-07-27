import CloseIcon from '@/components/@common/CloseIcon/CloseIcon';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { useTheme } from '@emotion/react';
import * as S from './ModalHeader.styled';

type Props = {
  title?: string;
  onClose: () => void;
  showCloseButton?: boolean;
};

const ModalHeader = ({ title, onClose, showCloseButton = true }: Props) => {
  const theme = useTheme();

  return (
    <S.Container>
      <Headline3>{title}</Headline3>
      {showCloseButton && (
        <S.CloseButton onClick={onClose}>
          <CloseIcon stroke={theme.color.gray[600]} />
        </S.CloseButton>
      )}
    </S.Container>
  );
};

export default ModalHeader;
