import { TemperatureOption } from '@/types/menu';
import TemperatureOptionButton from './TemperatureOptionButton/TemperatureOptionButton';
import * as S from './TemperatureToggle.styled';
import { RefObject, useEffect, useState } from 'react';
import ScreenReaderOnly from '../ScreenReaderOnly/ScreenReaderOnly';

type Props = {
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (option: TemperatureOption) => void;
  ref?: RefObject<HTMLDivElement | null>;
};

const TEMPERATURE_OPTIONS: TemperatureOption[] = ['ICE', 'HOT'];

const TemperatureToggle = ({ selectedTemperature, onChangeTemperature, ref }: Props) => {
  const [announcement, setAnnouncement] = useState('');

  useEffect(() => {
    setAnnouncement(`${selectedTemperature} 선택됨`);
  }, [selectedTemperature]);

  return (
    <S.Container ref={ref} tabIndex={0} role="group" aria-label="온도 선택">
      {TEMPERATURE_OPTIONS.map((option, index) => (
        <TemperatureOptionButton
          key={option}
          option={option}
          position={index === 0 ? 'left' : 'right'}
          selected={selectedTemperature === option}
          onClick={() => onChangeTemperature(option)}
        />
      ))}
      <ScreenReaderOnly>{announcement}</ScreenReaderOnly>
    </S.Container>
  );
};

export default TemperatureToggle;
