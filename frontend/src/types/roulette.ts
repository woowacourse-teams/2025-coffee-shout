import { ColorList } from '@/constants/color';
import { Player } from './player';

export type RouletteView = 'roulette' | 'statistics';

export type Probability = {
  playerResponse: Player;
  probability: number;
};

export type PlayerProbability = {
  playerName: string;
  probability: number;
  playerColor: ColorList;
};

export type ProbabilityHistory = {
  prev: PlayerProbability[];
  current: PlayerProbability[];
};

export type Angle = {
  playerName: string;
  startAngle: number;
  endAngle: number;
  playerColor: ColorList;
};
