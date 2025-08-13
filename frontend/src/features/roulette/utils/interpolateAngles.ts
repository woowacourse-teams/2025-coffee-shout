import { RouletteSector, PlayerProbability } from '@/types/roulette';
import { getPlayersWithAngles } from './getPlayerWithAngles.ts';

type Props = {
  from: PlayerProbability[];
  to: PlayerProbability[];
  progress: number;
};

export const interpolateAngles = ({ from, to, progress }: Props): RouletteSector[] => {
  const totalFrom = from.reduce((sum, p) => sum + p.probability, 0);
  const totalTo = to.reduce((sum, p) => sum + p.probability, 0);

  const fromAngles = getPlayersWithAngles(from, totalFrom);
  const toAngles = getPlayersWithAngles(to, totalTo);

  return fromAngles.map((fromPlayer, i) => {
    const toPlayer = toAngles[i];

    return {
      playerName: fromPlayer.playerName,
      startAngle: fromPlayer.startAngle + (toPlayer.startAngle - fromPlayer.startAngle) * progress,
      endAngle: fromPlayer.endAngle + (toPlayer.endAngle - fromPlayer.endAngle) * progress,
      playerColor: fromPlayer.playerColor,
    };
  });
};
