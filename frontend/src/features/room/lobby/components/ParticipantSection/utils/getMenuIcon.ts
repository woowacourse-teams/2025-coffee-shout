import { MenuType } from '@/types/menu';
import MenuIcon from '@/assets/juice.svg';

// @TODO: 아이콘 바꾸기
export const getMenuIcon = (menuType: MenuType) => {
  switch (menuType) {
    case 'COFFEE':
      return MenuIcon;
    case 'ADE':
      return MenuIcon;
    case 'SMOOTHIE':
      return MenuIcon;
    case 'FRAPPUCCINO':
      return MenuIcon;
    case 'ETC':
      return MenuIcon;
    default:
      return MenuIcon;
  }
};
