import PlayerIcon from '@/components/@composition/PlayerIcon/PlayerIcon';
import { ColorList } from '@/constants/color';
import { useRotationAnimation } from '../../hooks/useRotationAnimation';
import Description from '@/components/@common/Description/Description';
import * as S from './RacingPlayer.styled';

type Props = {
  playerName: string;
  position: number;
  speed: number;
  isMe: boolean;
  myPosition: number;
  color: ColorList;
};

const RacingPlayer = ({ position, speed, isMe, myPosition, color, playerName }: Props) => {
  const rotatingRef = useRotationAnimation({ speed });

  return (
    <S.Player $isMe={isMe} $position={position} $myPosition={myPosition}>
      <S.PlayerName>
        <Description color={isMe ? 'point-500' : 'white'}>{playerName}</Description>
      </S.PlayerName>

      <S.RotatingWrapper ref={rotatingRef}>
        <PlayerIcon color={color} />
      </S.RotatingWrapper>
    </S.Player>
  );
};

export default RacingPlayer;
