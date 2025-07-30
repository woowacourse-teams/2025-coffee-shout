type Player = {
  playerName: string;
  probability: number;
};

type PlayerWithAngle = {
  startAngle: number;
  endAngle: number;
} & Player;

export const getPlayersWithAngles = (
  players: Player[],
  totalProbability: number
): PlayerWithAngle[] => {
  let currentAngle = 0;
  return players.map((player) => {
    const angle = (player.probability / totalProbability) * 360;
    const startAngle = currentAngle;
    const endAngle = currentAngle + angle;
    currentAngle = endAngle;
    return { ...player, startAngle, endAngle };
  });
};
