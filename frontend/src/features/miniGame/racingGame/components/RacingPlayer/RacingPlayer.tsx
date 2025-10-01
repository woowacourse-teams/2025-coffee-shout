import React, { useEffect, useRef } from 'react';
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
  const rotatingRef = useRef<HTMLDivElement>(null);
  const angleRef = useRef(0);

  useEffect(() => {
    let frameId: number;
    let lastTime = performance.now();

    const update = (time: number) => {
      const delta = (time - lastTime) / 1000; // 초 단위
      lastTime = time;

      angleRef.current += speed * delta * 30; // 속도에 비례해 증가
      if (rotatingRef.current) {
        rotatingRef.current.style.transform = `rotate(${angleRef.current}deg)`;
      }

      frameId = requestAnimationFrame(update);
    };

    frameId = requestAnimationFrame(update);
    return () => cancelAnimationFrame(frameId);
  }, [speed]);

  return (
    <Player $isMe={isMe} $x={x} $myX={myX}>
      <RotatingWrapper ref={rotatingRef}>
        <PlayerIcon color={colorList[colorIndex % colorList.length]} />
      </RotatingWrapper>
    </Player>
  );
};

export default RacingPlayer;
