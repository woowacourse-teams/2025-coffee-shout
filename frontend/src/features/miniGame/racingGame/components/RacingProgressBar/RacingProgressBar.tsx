import * as S from './RacingProgressBar.styled';
import { colorList } from '@/constants/color';

interface Player {
  playerName: string;
  x: number;
}

interface RacingProgressBarProps {
  myName: string;
  endDistance: number;
  players: Player[];
}

const RacingProgressBar = ({ myName, endDistance, players }: RacingProgressBarProps) => {
  return (
    <S.Container>
      <S.ProgressTrack>
        {players.map((player, index) => {
          const progress = Math.min((player.x / endDistance) * 100, 100);
          const isMe = player.playerName === myName;
          const color = colorList[index % colorList.length];

          return (
            <S.ProgressFill
              key={`fill-${player.playerName}`}
              $progress={progress}
              $color={color}
              $isMe={isMe}
            />
          );
        })}
        {players.map((player, index) => {
          const progress = Math.min((player.x / endDistance) * 100, 100);
          const isMe = player.playerName === myName;
          const color = colorList[index % colorList.length];

          return (
            <S.ProgressMarker
              key={player.playerName}
              $progress={progress}
              $color={color}
              $isMe={isMe}
            />
          );
        })}
      </S.ProgressTrack>
    </S.Container>
  );
};

export default RacingProgressBar;
