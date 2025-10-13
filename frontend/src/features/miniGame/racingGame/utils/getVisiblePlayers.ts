// utils/getVisiblePlayers.ts
type Player = {
  playerName: string;
  speed: number;
  position: number;
};

const VISIBLE_PLAYER_COUNT = 7;
const HALF_COUNT = 3;

export const getVisiblePlayers = (players: Player[], myName: string): Player[] => {
  if (players.length === 0) return [];

  const totalPlayers = players.length;
  const myIndex = players.findIndex((player) => player.playerName === myName);

  if (myIndex === -1) return [];

  if (totalPlayers <= VISIBLE_PLAYER_COUNT) {
    const result: Player[] = [];

    for (let i = 0; i < totalPlayers; i++) {
      const offset = i - Math.floor(totalPlayers / 2);
      const playerIndex = (myIndex + offset + totalPlayers) % totalPlayers;
      result.push({ ...players[playerIndex] });
    }

    return result;
  }

  const result: Player[] = [];

  for (let i = 0; i < VISIBLE_PLAYER_COUNT; i++) {
    const offset = i - HALF_COUNT;
    const playerIndex = (myIndex + offset + totalPlayers) % totalPlayers;
    result.push({ ...players[playerIndex] });
  }

  return result;
};
