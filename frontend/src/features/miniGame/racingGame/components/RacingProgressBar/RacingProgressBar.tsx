import * as S from './RacingProgressBar.styled';
import { usePlayerProgressData } from '../../hooks/usePlayerProgressData';
import { colorList } from '@/constants/color';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';

type Player = {
  playerName: string;
  position: number;
};

type Props = {
  myName: string;
  endDistance: number;
  players: Player[];
};

const RacingProgressBar = ({ myName, endDistance, players }: Props) => {
  const playerProgressData = usePlayerProgressData({ players, endDistance, myName });
  const { getParticipantColorIndex } = useParticipants();

  return (
    <S.Container>
      <S.ProgressTrack>
        {playerProgressData.map(({ player, progress, isMe }) => (
          <S.ProgressFill
            key={`fill-${player.playerName}`}
            $progress={progress}
            $color={colorList[getParticipantColorIndex(player.playerName)]}
            $isMe={isMe}
          />
        ))}
        {playerProgressData.map(({ player, progress, isMe }) => (
          <S.ProgressMarker
            key={player.playerName}
            $progress={progress}
            $color={colorList[getParticipantColorIndex(player.playerName)]}
            $isMe={isMe}
          />
        ))}
      </S.ProgressTrack>
    </S.Container>
  );
};

export default RacingProgressBar;
