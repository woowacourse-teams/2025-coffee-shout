import { useTheme } from '@emotion/react';
import { RouletteSector, PlayerProbability } from '@/types/roulette';
import * as S from './RouletteWheel.styled';
import { WHEEL_CONFIG } from '../../constants/config';
import { convertProbabilitiesToAngles } from '../../utils';
import RouletteSlice from '../RouletteSlice/RouletteSlice';

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

  const playersWithAngles = sectors || convertProbabilitiesToAngles(playerProbabilities);

  return (
    <S.Container>
      <S.Pin />
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
              strokeColor={theme.color.point[100]}
            />
          ))}
        </svg>
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;
