import React from 'react';
import PlayerIcon from '@/components/@composition/PlayerIcon/PlayerIcon';
import { colorList } from '@/constants/color';
import { Player, RotatingWrapper } from './RacingPlayer.styled';
import { useRotationAnimation } from '../../hooks/useRotationAnimation';

type Props = {
  playerName: string;
  x: number;
  speed: number;
  isMe: boolean;
  myX: number;
  colorIndex: number;
};

const RacingPlayer = ({ x, speed, isMe, myX, colorIndex }: Props) => {
  const rotatingRef = useRotationAnimation({ speed });

  return (
    <Player $isMe={isMe} $x={x} $myX={myX}>
      <RotatingWrapper ref={rotatingRef}>
        <PlayerIcon color={colorList[colorIndex % colorList.length]} />
      </RotatingWrapper>
    </Player>
  );
};

export default RacingPlayer;
