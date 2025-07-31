export type Menu = {
  id: number;
  name: string;
  menuType: MenuType;
};

export type MenuType = 'COFFEE' | 'ADE' | 'SMOOTHIE' | 'FRAPPUCCINO' | 'ETC';
