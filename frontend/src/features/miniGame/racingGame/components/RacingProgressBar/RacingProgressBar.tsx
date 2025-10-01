import * as S from './RacingProgressBar.styled';
import { usePlayerProgressData } from '../../hooks/usePlayerProgressData';

type Player = {
  playerName: string;
  x: number;
};

type Props = {
  myName: string;
  endDistance: number;
  players: Player[];
};

const RacingProgressBar = ({ myName, endDistance, players }: Props) => {
  const playerProgressData = usePlayerProgressData({ players, endDistance, myName });

  return (
    <S.Container>
      <S.ProgressTrack>
        {playerProgressData.map(({ player, progress, color, isMe }) => (
          <S.ProgressFill
            key={`fill-${player.playerName}`}
            $progress={progress}
            $color={color}
            $isMe={isMe}
          />
        ))}
        {playerProgressData.map(({ player, progress, color, isMe }) => (
          <S.ProgressMarker
            key={player.playerName}
            $progress={progress}
            $color={color}
            $isMe={isMe}
          />
        ))}
      </S.ProgressTrack>
    </S.Container>
  );
};

export default RacingProgressBar;
