import MenuListItemSkeleton from '@/components/@composition/MenuListItemSkeleton/MenuListItemSkeleton';
import * as S from './MenuList.styled';
import { Menu } from '@/types/menu';
import { useMenus } from '../../hooks/useMenus';
import MenuListItem from '@/components/@common/MenuListItem/MenuListItem';

type Props = {
  categoryId: number | null;
  onClickMenu: (menu: Menu) => void;
};

const MenuList = ({ categoryId, onClickMenu }: Props) => {
  const { menus, loading } = useMenus(categoryId);

  return (
    <S.Container>
      {loading ? (
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
