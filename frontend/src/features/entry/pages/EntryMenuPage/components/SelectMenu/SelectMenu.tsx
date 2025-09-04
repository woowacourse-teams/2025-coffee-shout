import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import CoffeeIcon from '@/assets/coffee.svg';

const SelectMenu = () => {
  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard color="#eb63d4" text="커피" iconSrc={CoffeeIcon} />
        <S.MenuList>
          <MenuListItem text="아메카노" onClick={() => {}} />
          <MenuListItem text="카페라떼" onClick={() => {}} />
          <MenuListItem text="티" onClick={() => {}} />
          <MenuListItem text="카페모카" onClick={() => {}} />
          <MenuListItem text="카페모카" onClick={() => {}} />
        </S.MenuList>
      </S.Wrapper>
    </>
  );
};

export default SelectMenu;
