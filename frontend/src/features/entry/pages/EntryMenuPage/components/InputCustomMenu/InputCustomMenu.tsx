import { ChangeEvent, ReactNode } from 'react';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuIntput';
import * as S from './InputCustomMenu.styled';

type Props = {
  customMenuName: string | null;
  onChangeCustomMenuName: (customMenuName: string) => void;
  onClickDoneButton: () => void;
  isMenuInputCompleted: boolean;
  children?: ReactNode;
};

const InputCustomMenu = ({
  customMenuName,
  onChangeCustomMenuName,
  onClickDoneButton,
  isMenuInputCompleted,
  children,
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
        {isMenuInputCompleted && children}
      </S.Wrapper>
    </>
  );
};

export default InputCustomMenu;
