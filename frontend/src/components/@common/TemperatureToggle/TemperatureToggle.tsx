import { TemperatureOption } from './temperatureOption';
import TemperatureOptionButton from './TemperatureOptionButton/TemperatureOptionButton';
import * as S from './TemperatureToggle.styled';

type Props = {
  selectedTemperature: TemperatureOption;
  onClick: (option: TemperatureOption) => void;
};

const TemperatureToggle = ({ selectedTemperature, onClick }: Props) => {
  return (
    <S.Container>
      <TemperatureOptionButton
        option="ICED"
        position="left"
        selected={selectedTemperature === 'ICED'}
        onClick={() => onClick('ICED')}
      />
      <TemperatureOptionButton
        option="HOT"
        position="right"
        selected={selectedTemperature === 'HOT'}
        onClick={() => onClick('HOT')}
      />
    </S.Container>
  );
};

export default TemperatureToggle;
