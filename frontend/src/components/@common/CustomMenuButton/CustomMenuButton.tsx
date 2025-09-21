import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import * as S from './CustomMenuButton.styled';
import WriteIcon from '@/assets/write-icon.svg';

interface Props {
  onClick: () => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();
  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
      onTouchStart={startTouchPress}
      onTouchEnd={endTouchPress}
    >
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
