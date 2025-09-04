import { TemperatureOption } from './temperatureOption';
import TemperatureOptionButton from './TemperatureOptionButton/TemperatureOptionButton';
import * as S from './TemperatureToggle.styled';

type Props = {
  selectedTemperature: TemperatureOption;
  onClick: (option: TemperatureOption) => void;
};

const TEMPERATURE_OPTIONS: TemperatureOption[] = ['ICED', 'HOT'];

const TemperatureToggle = ({ selectedTemperature, onClick }: Props) => {
  return (
    <S.Container>
      {TEMPERATURE_OPTIONS.map((option, index) => (
        <TemperatureOptionButton
          key={option}
          option={option}
          position={index === 0 ? 'left' : 'right'}
          selected={selectedTemperature === option}
          onClick={() => onClick(option)}
        />
      ))}
    </S.Container>
  );
};

export default TemperatureToggle;
