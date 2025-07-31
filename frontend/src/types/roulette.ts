import { Player } from './player';

export type RouletteView = 'roulette' | 'statistics';

export type Probability = {
  playerResponse: Player;
  probability: number;
};

export type PlayerProbability = {
  playerName: string;
  probability: number;
};
