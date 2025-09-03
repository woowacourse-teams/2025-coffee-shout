import * as S from './TemperatureToggleButton.styled';

type Props = {
  option: 'HOT' | 'ICED';
  position: 'left' | 'right';
  selected: boolean;
};

const TemperatureOptionButton = ({ option, position, selected }: Props) => {
  return (
    <S.Container $position={position} $selected={selected} $option={option}>
      {option}
    </S.Container>
  );
};

export default TemperatureOptionButton;
