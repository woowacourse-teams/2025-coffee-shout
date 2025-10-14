import * as S from './RacingProgressBar.styled';
import { usePlayerProgressData } from '../../hooks/usePlayerProgressData';
import { colorList } from '@/constants/color';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { RacingPlayer } from '@/types/miniGame/racingGame';

type Props = {
  myName: string;
  endDistance: number;
  players: RacingPlayer[];
};

const RacingProgressBar = ({ myName, endDistance, players }: Props) => {
  const playerProgressData = usePlayerProgressData({ players, endDistance, myName });
  const { getParticipantColorIndex } = useParticipants();

  return (
    <S.Container>
      <S.ProgressTrack>
        {playerProgressData.map(({ player, progress, isMe }) => [
          <S.ProgressFill
            key={`fill-${player.playerName}`}
            $progress={progress}
            $color={colorList[getParticipantColorIndex(player.playerName)]}
            $isMe={isMe}
          />,
          <S.ProgressMarker
            key={`marker-${player.playerName}`}
            $progress={progress}
            $color={colorList[getParticipantColorIndex(player.playerName)]}
            $isMe={isMe}
          />,
        ])}
      </S.ProgressTrack>
    </S.Container>
  );
};

export default RacingProgressBar;
