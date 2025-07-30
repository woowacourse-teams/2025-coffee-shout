export type Menu = {
  id: number;
  name: string;
  menuType: MenuType;
};

type MenuType = 'COFFEE' | 'ADE' | 'SMOOTHIE' | 'FRAPPUCCINO' | 'ETC';
