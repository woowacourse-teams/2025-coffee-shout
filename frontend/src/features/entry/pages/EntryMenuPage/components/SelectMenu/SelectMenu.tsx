import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import { CategoryWithColor, Menu } from '@/types/menu';
import { PropsWithChildren } from 'react';
import useFetch from '@/apis/rest/useFetch';

type Props = {
  onMenuSelect: (menu: Menu) => void;
  selectedCategory: CategoryWithColor;
  selectedMenu: Menu | null;
} & PropsWithChildren;

const SelectMenu = ({ onMenuSelect, selectedCategory, selectedMenu, children }: Props) => {
  const { data: menus = [] } = useFetch<Menu[]>({
    endpoint: `/menu-categories/${selectedCategory.id}/menus`,
  });

  const handleClickMenu = (menu: Menu) => {
    onMenuSelect(menu);
  };

  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard
          color={selectedCategory.color}
          text={selectedCategory.name}
          imageUrl={selectedCategory.imageUrl}
        />
        {!selectedMenu && (
          <S.MenuListWrapper>
            <S.MenuList>
              {menus.length === 0 ? (
                <S.MenuListEmpty>메뉴 정보가 없습니다.</S.MenuListEmpty>
              ) : (
                menus.map((menu) => (
                  <MenuListItem
                    key={menu.id}
                    text={menu.name}
                    onClick={() => handleClickMenu(menu)}
                  />
                ))
              )}
            </S.MenuList>
          </S.MenuListWrapper>
        )}
        {children}
      </S.Wrapper>
    </>
  );
};

export default SelectMenu;
