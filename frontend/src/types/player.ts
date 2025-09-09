import { PlayerMenu } from './menu';

export type PlayerType = 'HOST' | 'GUEST';

export type Player = {
  playerName: string;
  menuResponse: PlayerMenu;
  playerType: PlayerType;
  isReady: boolean;
  colorIndex: number;
};
