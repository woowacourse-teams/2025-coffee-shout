import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import TemperatureOnly from '@/components/@common/TemperatureOnly/TemperatureOnly';
import { TemperatureAvailability, TemperatureOption } from '@/types/menu';
import { useEffect, useRef } from 'react';

type Props = {
  temperatureAvailability: TemperatureAvailability;
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (temperature: TemperatureOption) => void;
};

const TEMPERATURE_AVAILABILITY_MAP: Record<
  Exclude<TemperatureAvailability, 'BOTH'>,
  TemperatureOption
> = {
  HOT_ONLY: 'HOT',
  ICE_ONLY: 'ICE',
} as const;

const SelectTemperature = ({
  temperatureAvailability,
  selectedTemperature,
  onChangeTemperature,
}: Props) => {
  const liveRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (liveRef.current) {
      liveRef.current.focus();
    }
  }, []);

  if (temperatureAvailability === 'BOTH') {
    return (
      <TemperatureToggle
        ref={liveRef}
        selectedTemperature={selectedTemperature}
        onChangeTemperature={onChangeTemperature}
      />
    );
  }

  return <TemperatureOnly temperature={TEMPERATURE_AVAILABILITY_MAP[temperatureAvailability]} />;
};

export default SelectTemperature;
