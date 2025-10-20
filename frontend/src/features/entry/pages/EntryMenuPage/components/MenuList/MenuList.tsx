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
        menus.map((menu, index) => {
          const isLast = index === menus.length - 1;
          return (
            <MenuListItem
              key={menu.id}
              text={menu.name}
              onClick={() => onClickMenu(menu)}
              ariaLabel={`${menu.name} 선택, ${index + 1}번째 메뉴${isLast ? ', 마지막 메뉴' : ''}`}
            />
          );
        })
      )}
    </S.Container>
  );
};

export default MenuList;
