import { useTheme } from '@emotion/react';
import { RouletteSector, PlayerProbability } from '@/types/roulette';
import * as S from './RouletteWheel.styled';
import { WHEEL_CONFIG } from '../../constants/config';
import { convertProbabilitiesToAngles } from '../../utils';
import RouletteSlice from '../RouletteSlice/RouletteSlice';
import { memo } from 'react';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props =
  | {
      sectors: RouletteSector[];
      playerProbabilities?: never;
      isSpinning?: boolean;
      finalRotation?: number;
    }
  | {
      sectors?: never;
      playerProbabilities: PlayerProbability[];
      isSpinning?: boolean;
      finalRotation?: number;
    };

const RouletteWheel = ({
  sectors,
  playerProbabilities,
  isSpinning = false,
  finalRotation = 0,
}: Props) => {
  const theme = useTheme();
  const { myName } = useIdentifier();

  const playersWithAngles = sectors || convertProbabilitiesToAngles(playerProbabilities);

  return (
    <S.Container>
      <Pin />
      <S.Wrapper $isSpinning={isSpinning} $finalRotation={finalRotation}>
        <svg
          width={WHEEL_CONFIG.SIZE}
          height={WHEEL_CONFIG.SIZE}
          viewBox={`0 0 ${WHEEL_CONFIG.SIZE} ${WHEEL_CONFIG.SIZE}`}
        >
          {playersWithAngles.map((player) => (
            <RouletteSlice
              key={player.playerName}
              player={player}
              strokeColor={player.playerName === myName ? theme.color.point[300] : 'transparent'}
            />
          ))}
        </svg>
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;

const Pin = memo(() => <S.Pin />);
Pin.displayName = 'Pin';
