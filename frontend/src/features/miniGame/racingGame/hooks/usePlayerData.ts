import { useMemo } from 'react';

type Player = {
  playerName: string;
  x: number;
  speed: number;
};

type Props = {
  players: Player[];
  myName: string;
};

export const usePlayerData = ({ players, myName }: Props) => {
  const myPlayer = useMemo(
    () => players.find((player) => player.playerName === myName),
    [players, myName]
  );

  const myX = myPlayer?.x ?? 0;
  const mySpeed = myPlayer?.speed ?? 0;

  return {
    myPlayer,
    myX,
    mySpeed,
  };
};
