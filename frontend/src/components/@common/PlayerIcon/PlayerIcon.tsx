import { ColorList } from '@/constants/color';
import * as S from './PlayerIcon.styled';
import CoffeeCharacter from '@/assets/coffee-character.svg';

type Props = {
  color: ColorList;
};

const PlayerIcon = ({ color }: Props) => {
  return (
    <S.Container color={color}>
      <S.Icon src={CoffeeCharacter} alt="player-icon" />
    </S.Container>
  );
};

export default PlayerIcon;
