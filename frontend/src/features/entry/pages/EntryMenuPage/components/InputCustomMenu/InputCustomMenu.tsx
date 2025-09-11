import { ChangeEvent, PropsWithChildren } from 'react';
import Headline3 from '@/components/@common/Headline3/Headline3';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import CustomMenuInput from '@/components/@common/CustomMenuInput/CustomMenuIntput';
import CustomMenuIcon from '@/assets/custom-menu-icon.svg';
import * as S from './InputCustomMenu.styled';

type Props = {
  customMenuName: string | null;
  onChangeCustomMenuName: (customMenuName: string) => void;
  onClickDoneButton: () => void;
  isMenuInputCompleted: boolean;
} & PropsWithChildren;

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
        <SelectionCard color="#eb63d4" text="직접 입력" imageUrl={CustomMenuIcon} />
        {!isMenuInputCompleted && (
          <CustomMenuInput
            placeholder="메뉴를 입력해주세요"
            value={customMenuName || ''}
            onChange={handleChangeCustomMenuInput}
            onClickDoneButton={onClickDoneButton}
          />
        )}
        {children}
      </S.Wrapper>
    </>
  );
};

export default InputCustomMenu;
