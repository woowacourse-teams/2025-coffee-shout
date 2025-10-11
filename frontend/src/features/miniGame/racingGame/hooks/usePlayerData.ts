import { useMemo } from 'react';

type Player = {
  playerName: string;
  position: number; 
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

  const myPosition = myPlayer?.position ?? 0;
  const mySpeed = myPlayer?.speed ?? 0;

  return {
    myPlayer,
    myPosition,
    mySpeed,
  };
};
