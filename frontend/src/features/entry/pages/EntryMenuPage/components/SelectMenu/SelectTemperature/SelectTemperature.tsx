import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import TemperatureOnly from '@/components/@common/TemperatureOnly/TemperatureOnly';
import { Menu, TemperatureAvailability, TemperatureOption } from '@/types/menu';

type Props = {
  selectedMenu: Menu;
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

const SelectTemperature = ({ selectedMenu, selectedTemperature, onChangeTemperature }: Props) => {
  return (
    <>
      <SelectionCard color="rgb(255, 220, 249)" text={selectedMenu.name} />
      {selectedMenu.temperatureAvailability === 'BOTH' ? (
        <TemperatureToggle
          selectedTemperature={selectedTemperature}
          onChangeTemperature={onChangeTemperature}
        />
      ) : (
        <TemperatureOnly
          temperature={TEMPERATURE_AVAILABILITY_MAP[selectedMenu.temperatureAvailability]}
        />
      )}
    </>
  );
};

export default SelectTemperature;
