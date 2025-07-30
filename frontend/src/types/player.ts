import { Menu } from './menu';

export type PlayerType = 'HOST' | 'GUEST';

export type IconColor = 'red';

export type Player = {
  playerName: string;
  menuResponse: Menu;
  playerType: PlayerType;
};
