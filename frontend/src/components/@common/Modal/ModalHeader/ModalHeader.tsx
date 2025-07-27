import CloseIcon from '@/assets/close.svg';
import Headline3 from '@/components/@common/Headline3/Headline3';
import * as S from './ModalHeader.styled';

type Props = {
  id?: string;
  title?: string;
  onClose: () => void;
  showCloseButton?: boolean;
};

const ModalHeader = ({ id, title, onClose, showCloseButton = true }: Props) => {
  return (
    <S.Container>
      <Headline3 id={id}>{title}</Headline3>
      {showCloseButton && (
        <S.CloseButton onClick={onClose}>
          <S.CloseIcon src={CloseIcon} alt="close-icon" />
        </S.CloseButton>
      )}
    </S.Container>
  );
};

export default ModalHeader;
