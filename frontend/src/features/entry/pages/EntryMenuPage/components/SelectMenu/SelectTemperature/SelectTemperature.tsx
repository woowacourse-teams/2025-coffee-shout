import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';

const SelectTemperature = () => {
  return (
    <>
      <SelectionCard color="#ffebfc" text="아메리카노" />
      <TemperatureToggle selectedTemperature="ICE" onChangeTemperature={() => {}} />
    </>
  );
};

export default SelectTemperature;
