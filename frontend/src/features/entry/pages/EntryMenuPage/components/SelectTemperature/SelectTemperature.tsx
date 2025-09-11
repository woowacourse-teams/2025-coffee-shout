import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import TemperatureOnly from '@/components/@common/TemperatureOnly/TemperatureOnly';
import { TemperatureAvailability, TemperatureOption } from '@/types/menu';

type Props = {
  menuName: string;
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
  menuName,
  temperatureAvailability,
  selectedTemperature,
  onChangeTemperature,
}: Props) => {
  return (
    <>
      <SelectionCard color="rgb(255, 220, 249)" text={menuName} />
      {temperatureAvailability === 'BOTH' ? (
        <TemperatureToggle
          selectedTemperature={selectedTemperature}
          onChangeTemperature={onChangeTemperature}
        />
      ) : (
        <TemperatureOnly temperature={TEMPERATURE_AVAILABILITY_MAP[temperatureAvailability]} />
      )}
    </>
  );
};

export default SelectTemperature;
