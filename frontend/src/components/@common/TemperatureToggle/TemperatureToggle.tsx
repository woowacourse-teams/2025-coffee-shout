import { TemperatureOption } from './temperatureOption';
import TemperatureOptionButton from './TemperatureOptionButton/TemperatureOptionButton';
import * as S from './TemperatureToggle.styled';

type Props = {
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (option: TemperatureOption) => void;
};

const TEMPERATURE_OPTIONS: TemperatureOption[] = ['ICED', 'HOT'];

const TemperatureToggle = ({ selectedTemperature, onChangeTemperature }: Props) => {
  return (
    <S.Container>
      {TEMPERATURE_OPTIONS.map((option, index) => (
        <TemperatureOptionButton
          key={option}
          option={option}
          position={index === 0 ? 'left' : 'right'}
          selected={selectedTemperature === option}
          onClick={() => onChangeTemperature(option)}
        />
      ))}
    </S.Container>
  );
};

export default TemperatureToggle;
