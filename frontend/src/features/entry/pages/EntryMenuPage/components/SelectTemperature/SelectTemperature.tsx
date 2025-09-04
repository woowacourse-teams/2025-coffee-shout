import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import * as S from './SelectTemperature.styled';
import CoffeeIcon from '@/assets/coffee.svg';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';

const SelectTemperature = () => {
  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard color="#eb63d4" text="커피" iconSrc={CoffeeIcon} />
        <SelectionCard color="#ffebfc" text="아메리카노" />
        <TemperatureToggle selectedTemperature="ICED" onChangeTemperature={() => {}} />
      </S.Wrapper>
    </>
  );
};

export default SelectTemperature;
