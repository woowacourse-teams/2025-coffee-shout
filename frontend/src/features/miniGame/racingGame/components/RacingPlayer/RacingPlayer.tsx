import React from 'react';
import PlayerIcon from '@/components/@composition/PlayerIcon/PlayerIcon';
import { colorList } from '@/constants/color';
import { Player, RotatingWrapper } from './RacingPlayer.styled';

interface RacingPlayerProps {
  playerName: string;
  x: number;
  speed: number;
  isMe: boolean;
  myX: number;
  colorIndex: number;
}

const RacingPlayer = ({ x, speed, isMe, myX, colorIndex }: RacingPlayerProps) => {
  return (
    <Player $isMe={isMe} $x={x} $myX={myX}>
      <RotatingWrapper $speed={speed}>
        <PlayerIcon color={colorList[colorIndex % colorList.length]} />
      </RotatingWrapper>
    </Player>
  );
};

export default RacingPlayer;
