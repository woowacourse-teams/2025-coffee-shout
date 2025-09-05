import Headline3 from '@/components/@common/Headline3/Headline3';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import * as S from './SelectMenu.styled';
import SelectionCard from '@/components/@common/SelectionCard/SelectionCard';
import CoffeeIcon from '@/assets/coffee.svg';
import { Category, NewMenu } from '@/types/menu';
import { useEffect, useState } from 'react';
import { api } from '@/apis/rest/api';
import TemperatureToggle from '@/components/@common/TemperatureToggle/TemperatureToggle';
import { TemperatureOption } from '@/components/@common/TemperatureToggle/temperatureOption';

type Props = {
  onClickMenu: (menu: NewMenu) => void;
  selectedCategory: Category;
  selectedMenu: NewMenu | null;
  selectedTemperature: TemperatureOption;
  onChangeTemperature: (temperature: TemperatureOption) => void;
};

const SelectMenu = ({
  onClickMenu,
  selectedCategory,
  selectedMenu,
  selectedTemperature,
  onChangeTemperature,
}: Props) => {
  const [menus, setMenus] = useState<NewMenu[]>([]);

  useEffect(() => {
    // (async () => {
    //   const menus = await api.get<NewMenu[]>(`/menu-categories/${selectedCategory}/menus`);
    //   setMenus(menus);
    // })();
    setMenus([
      {
        id: 1,
        name: '아메리카노',
        temperatureAvailability: 'ICE_ONLY',
      },
      {
        id: 2,
        name: '카페라떼',
        temperatureAvailability: 'ICE_ONLY',
      },
    ]);
  }, [selectedCategory]);

  return (
    <>
      <Headline3>메뉴를 선택해주세요</Headline3>
      <S.Wrapper>
        <SelectionCard
          color="#eb63d4"
          text={selectedCategory.name}
          imgUrl={selectedCategory.imgUrl}
        />
        {!selectedMenu && (
          <S.MenuList>
            {menus.map((menu) => (
              <MenuListItem key={menu.id} text={menu.name} onClick={() => onClickMenu(menu)} />
            ))}
          </S.MenuList>
        )}
        {selectedMenu && (
          <>
            <SelectionCard color="rgb(255, 220, 249)" text={selectedMenu.name} />
            <TemperatureToggle
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
