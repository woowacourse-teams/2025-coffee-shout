import { PlayerProbability } from '@/types/roulette';
import { getPlayersWithAngles } from './getPlayerWithAngles.ts';

export const convertProbabilitiesToAngles = (playerProbabilities: PlayerProbability[]) => {
  const totalProbability = playerProbabilities.reduce((sum, player) => sum + player.probability, 0);
  return getPlayersWithAngles(playerProbabilities, totalProbability);
};
