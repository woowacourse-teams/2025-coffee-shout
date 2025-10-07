import { useMemo } from 'react';

type Player = {
  playerName: string;
  position: number; // 서버에서 position으로 보내고 있음
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
