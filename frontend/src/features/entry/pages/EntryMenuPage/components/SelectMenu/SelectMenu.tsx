import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import { Category, Menu } from '@/types/menu';
import { useEffect, useState, ReactNode } from 'react';
import { api } from '@/apis/rest/api';

type Props = {
  onMenuSelect: (menu: Menu) => void;
  selectedCategory: Category;
  selectedMenu: Menu | null;
  children?: ReactNode;
};

const SelectMenu = ({ onMenuSelect, selectedCategory, selectedMenu, children }: Props) => {
  const [menus, setMenus] = useState<Menu[]>([]);

  useEffect(() => {
    (async () => {
      const menus = await api.get<Menu[]>(`/menu-categories/${selectedCategory.id}/menus`);
      setMenus(menus);
    })();
  }, [selectedCategory]);

  const handleClickMenu = (menu: Menu) => {
    onMenuSelect(menu);
  };

  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard
          color="#eb63d4"
          text={selectedCategory.name}
          imageUrl={selectedCategory.imageUrl}
        />
        {!selectedMenu && (
          <S.MenuList>
            {menus.map((menu) => (
              <MenuListItem key={menu.id} text={menu.name} onClick={() => handleClickMenu(menu)} />
            ))}
          </S.MenuList>
        )}
        {selectedMenu && children}
      </S.Wrapper>
    </>
  );
};

export default SelectMenu;
