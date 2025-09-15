import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import TemperatureOnly from '@/components/@common/TemperatureOnly/TemperatureOnly';
import { TemperatureAvailability, TemperatureOption } from '@/types/menu';
import { theme } from '@/styles/theme';

type Props = {
  menuName: string;
  temperatureAvailability: TemperatureAvailability;
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (temperature: TemperatureOption) => void;
  selectionCardColor?: string;
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
  selectionCardColor = theme.color.point[50],
}: Props) => {
  return (
    <>
      <SelectionCard color={selectionCardColor} text={menuName} />
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
