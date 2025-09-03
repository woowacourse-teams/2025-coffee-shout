import TemperatureOptionButton from './TemperatureOptionButton/TemperatureOptionButton';
import * as S from './TemperatureToggle.styled';

type Props = {
  selectedTemperature: 'HOT' | 'ICED';
};

const TemperatureToggle = ({ selectedTemperature }: Props) => {
  return (
    <S.Container>
      <TemperatureOptionButton
        option="ICED"
        position="left"
        selected={selectedTemperature === 'ICED'}
      />
      <TemperatureOptionButton
        option="HOT"
        position="right"
        selected={selectedTemperature === 'HOT'}
      />
    </S.Container>
  );
};

export default TemperatureToggle;
