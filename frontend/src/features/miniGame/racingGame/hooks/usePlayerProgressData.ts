import { useMemo } from 'react';
import { colorList } from '@/constants/color';

const MAX_PROGRESS_PERCENTAGE = 100;

type Player = {
  playerName: string;
  x: number;
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
  color: string;
  index: number;
};

export const usePlayerProgressData = ({ players, endDistance, myName }: Props) => {
  const playerProgressData = useMemo((): PlayerProgressData[] => {
    return players.map((player, index) => {
      const progress = Math.min(
        (player.x / endDistance) * MAX_PROGRESS_PERCENTAGE,
        MAX_PROGRESS_PERCENTAGE
      );
      const isMe = player.playerName === myName;
      const color = colorList[index % colorList.length];

      return {
        player,
        progress,
        isMe,
        color,
        index,
      };
    });
  }, [players, endDistance, myName]);

  return playerProgressData;
};
