import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import * as S from './CustomMenuButton.styled';
import WriteIcon from '@/assets/write-icon.svg';

interface Props {
  onClick: () => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  const { touchState, handleTouchDown, handleTouchUp } = useTouchInteraction();

  return (
    <S.Container
      onPointerDown={handleTouchDown}
      onPointerUp={(e) => {
        handleTouchUp(e);
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
