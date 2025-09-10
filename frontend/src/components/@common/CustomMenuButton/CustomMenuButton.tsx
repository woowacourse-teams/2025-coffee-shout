import * as S from './CustomMenuButton.styled';
import WriteIcon from '@/assets/write-icon.svg';

interface Props {
  onClick: () => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  return (
    <S.Container onClick={onClick}>
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
