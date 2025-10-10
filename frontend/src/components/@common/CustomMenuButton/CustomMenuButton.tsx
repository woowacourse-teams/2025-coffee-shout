import WriteIcon from '@/assets/write-icon.svg';
import { usePressAnimation } from '@/hooks/usePressAnimation';

import * as S from './CustomMenuButton.styled';

interface Props {
  onClick: () => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container
      onPointerDown={onPointerDown}
      onPointerUp={(e) => {
        onPointerUp(e);
        onClick();
      }}
      $touchState={touchState}
    >
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
