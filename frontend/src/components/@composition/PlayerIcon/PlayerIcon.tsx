import { ColorList } from '@/constants/color';
import CoffeeCharacter from '@/assets/coffee-character.svg';
import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';

type Props = {
  color: ColorList;
};

const PlayerIcon = ({ color }: Props) => {
  return <CircleIcon color={color} imgUrl={CoffeeCharacter} iconAlt="player-icon" />;
};

export default PlayerIcon;
