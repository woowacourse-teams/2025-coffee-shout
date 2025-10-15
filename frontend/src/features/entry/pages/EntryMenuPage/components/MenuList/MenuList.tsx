import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';
import MenuListItemSkeleton from '@/components/@composition/MenuListItemSkeleton/MenuListItemSkeleton';
import * as S from './MenuList.styled';
import { Menu } from '@/types/menu';

type Props = {
  menus: Menu[];
  isMenusLoading: boolean;
  onClickMenu: (menu: Menu) => void;
};

const MenuList = ({ menus, isMenusLoading, onClickMenu }: Props) => {
  return (
    <S.Container>
      {isMenusLoading ? (
        <MenuListItemSkeleton />
      ) : (
        menus.map((menu) => (
          <MenuListItem key={menu.id} text={menu.name} onClick={() => onClickMenu(menu)} />
        ))
      )}
    </S.Container>
  );
};

export default MenuList;
