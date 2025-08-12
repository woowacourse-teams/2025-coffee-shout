import { Angle, PlayerProbability } from '@/types/roulette';
import { convertProbabilitiesToAngles } from './convertProbabilitiesToAngles';

type Props = {
  from: PlayerProbability[];
  to: PlayerProbability[];
  t: number;
};

export const interpolateAngles = ({ from, to, t }: Props): Angle[] => {
  const fromAngles = convertProbabilitiesToAngles(from);
  const toAngles = convertProbabilitiesToAngles(to);

  return fromAngles.map((fromPlayer, i) => {
    const toPlayer = toAngles[i];

    return {
      playerName: fromPlayer.playerName,
      startAngle: fromPlayer.startAngle + (toPlayer.startAngle - fromPlayer.startAngle) * t,
      endAngle: fromPlayer.endAngle + (toPlayer.endAngle - fromPlayer.endAngle) * t,
      playerColor: fromPlayer.playerColor,
    };
  });
};
