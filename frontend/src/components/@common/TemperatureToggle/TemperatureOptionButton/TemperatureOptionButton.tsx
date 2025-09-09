import { TemperatureOption } from '@/types/menu';
import * as S from './TemperatureToggleButton.styled';

type Props = {
  option: TemperatureOption;
  position: 'left' | 'right';
  selected: boolean;
  onClick: () => void;
};

const TemperatureOptionButton = ({ option, position, selected, onClick }: Props) => {
  return (
    <S.Container $position={position} $selected={selected} $option={option} onClick={onClick}>
      {option}
    </S.Container>
  );
};

export default TemperatureOptionButton;
