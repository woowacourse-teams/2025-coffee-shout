import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import { Category, Menu, TemperatureAvailability } from '@/types/menu';
import { useEffect, useState } from 'react';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import { TemperatureOption } from '@/types/menu';
import TemperatureOnly from '@/components/@common/TemperatureOnly/TemperatureOnly';
import { api } from '@/apis/rest/api';

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

  const TEMPERATURE_AVAILABILITY_MAP: Record<
    Exclude<TemperatureAvailability, 'BOTH'>,
    TemperatureOption
  > = {
    HOT_ONLY: 'HOT',
    ICE_ONLY: 'ICE',
  } as const;

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
        )}
      </S.Wrapper>
    </>
  );
};

export default SelectMenu;
