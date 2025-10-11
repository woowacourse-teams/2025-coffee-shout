import { useMemo } from 'react';

const MAX_PROGRESS_PERCENTAGE = 100;

type Player = {
  playerName: string;
  position: number; 
};

type Props = {
  players: Player[];
  endDistance: number;
  myName: string;
};

type PlayerProgressData = {
  player: Player;
  progress: number;
  isMe: boolean;
  index: number;
};

export const usePlayerProgressData = ({ players, endDistance, myName }: Props) => {
  const playerProgressData = useMemo((): PlayerProgressData[] => {
    return players.map((player, index) => {
      const progress = Math.min(
        (player.position / endDistance) * MAX_PROGRESS_PERCENTAGE,
        MAX_PROGRESS_PERCENTAGE
      );
      const isMe = player.playerName === myName;

      return {
        player,
        progress,
        isMe,
        index,
      };
    });
  }, [players, endDistance, myName]);

  return playerProgressData;
};
