import WriteIcon from '@/assets/write-icon.svg';
import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import * as S from './CustomMenuButton.styled';

interface Props {
  onClick: () => void;
}

const CustomMenuButton = ({ onClick }: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container {...pointerHandlers} $touchState={touchState}>
      <S.Icon src={WriteIcon} alt="직접 입력" />
      <S.Text>직접 입력</S.Text>
    </S.Container>
  );
};

export default CustomMenuButton;
