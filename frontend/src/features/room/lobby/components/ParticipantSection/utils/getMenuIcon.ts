import { MenuType } from '@/types/menu';
import Coffee from '@/assets/coffee.svg';
import Ade from '@/assets/ade.svg';
import Smoothie from '@/assets/smoothie.svg';
import Frappuccino from '@/assets/frappuccino.svg';
import ETC from '@/assets/etc.svg';

export const getMenuIcon = (menuType: MenuType) => {
  switch (menuType) {
    case 'COFFEE':
      return Coffee;
    case 'ADE':
      return Ade;
    case 'SMOOTHIE':
      return Smoothie;
    case 'FRAPPUCCINO':
      return Frappuccino;
    case 'ETC':
      return ETC;
    default:
      return ETC;
  }
};
