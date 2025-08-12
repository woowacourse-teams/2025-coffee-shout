import { Angle } from '@/types/roulette';

export const calculateFinalRotation = (finalAngles: Angle[], winner: string | null) => {
  if (!winner) return 0;
  const winnerData = finalAngles.find((player) => player.playerName === winner);
  if (!winnerData) return 0;

  const { startAngle, endAngle } = winnerData;

  const centerAngle = (startAngle + endAngle) / 2;
  const finalRotation = 360 - centerAngle;
  return normalize(finalRotation);
};

const normalize = (deg: number) => ((deg % 360) + 360) % 360;
