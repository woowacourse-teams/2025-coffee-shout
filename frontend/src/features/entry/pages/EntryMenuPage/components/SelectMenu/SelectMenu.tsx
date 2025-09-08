import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import { Category, Menu, TemperatureOption } from '@/types/menu';
import { useEffect, useState } from 'react';
import { api } from '@/apis/rest/api';
import SelectTemperature from './SelectTemperature/SelectTemperature';

type Props = {
  onMenuSelect: (menu: Menu) => void;
  selectedCategory: Category;
  selectedMenu: Menu | null;
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (temperature: TemperatureOption) => void;
};

const SelectMenu = ({
  onMenuSelect,
  selectedCategory,
  selectedMenu,
  selectedTemperature,
  onChangeTemperature,
}: Props) => {
  const [menus, setMenus] = useState<Menu[]>([]);

  useEffect(() => {
    (async () => {
      const menus = await api.get<Menu[]>(`/menu-categories/${selectedCategory}/menus`);
      setMenus(menus);
    })();
  }, [selectedCategory]);

  const handleClickMenu = (menu: Menu) => {
    onMenuSelect(menu);
    if (menu.temperatureAvailability === 'ICE_ONLY') {
      onChangeTemperature('ICE');
    } else if (menu.temperatureAvailability === 'HOT_ONLY') {
      onChangeTemperature('HOT');
    }
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
        {selectedMenu && (
          <>
            <SelectTemperature
              selectedMenu={selectedMenu}
              selectedTemperature={selectedTemperature}
              onChangeTemperature={onChangeTemperature}
            />
          </>
        )}
      </S.Wrapper>
    </>
  );
};

export default SelectMenu;
