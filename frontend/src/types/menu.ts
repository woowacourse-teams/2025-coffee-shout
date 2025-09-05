export type Menu = {
  id: number;
  name: string;
  menuType: MenuType;
};

export type NewMenu = {
  id: number;
  name: string;
  temperatureAvailability: 'ICE_ONLY' | 'HOT_ONLY' | 'BOTH';
};

export type MenuType = 'COFFEE' | 'ADE' | 'SMOOTHIE' | 'FRAPPUCCINO' | 'ETC';

export type Category = {
  id: number;
  name: string;
  imgUrl: string;
};

export type TemperatureOption = 'HOT' | 'ICE';
