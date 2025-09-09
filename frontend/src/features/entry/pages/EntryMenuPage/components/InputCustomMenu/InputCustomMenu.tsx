import { ChangeEvent } from 'react';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuIntput';
import * as S from './InputCustomMenu.styled';
import SelectTemperature from '../SelectTemperature/SelectTemperature';
import { TemperatureOption } from '@/types/menu';

type Props = {
  customMenuName: string | null;
  onChangeCustomMenuName: (customMenuName: string) => void;
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (temperature: TemperatureOption) => void;
  onClickDoneButton: () => void;
  isMenuInputCompleted: boolean;
};

const InputCustomMenu = ({
  customMenuName,
  onChangeCustomMenuName,
  selectedTemperature,
  onChangeTemperature,
  onClickDoneButton,
  isMenuInputCompleted,
}: Props) => {
  const handleChangeCustomMenuInput = (e: ChangeEvent<HTMLInputElement>) => {
    onChangeCustomMenuName(e.target.value);
  };

  return (
    <>
      <Headline3>메뉴를 입력해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard color="#eb63d4" text="직접 입력" imageUrl={undefined} />
        {!isMenuInputCompleted && (
          <CustomMenuInput
            placeholder="메뉴를 입력해주세요"
            value={customMenuName || ''}
            onChange={handleChangeCustomMenuInput}
            onClickDoneButton={onClickDoneButton}
          />
        )}
        {isMenuInputCompleted && (
          <SelectTemperature
            menuName={customMenuName || ''}
            temperatureAvailability={'BOTH'}
            selectedTemperature={selectedTemperature}
            onChangeTemperature={onChangeTemperature}
          />
        )}
      </S.Wrapper>
    </>
  );
};

export default InputCustomMenu;
