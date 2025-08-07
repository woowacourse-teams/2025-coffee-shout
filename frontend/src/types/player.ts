import { Menu } from './menu';

export type PlayerType = 'HOST' | 'GUEST';

export type Player = {
  playerName: string;
  menuResponse: Menu;
  playerType: PlayerType;
  isReady: boolean;
  colorIndex: number;
};
