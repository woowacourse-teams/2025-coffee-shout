import { useTheme } from '@emotion/react';
import { RouletteSector, PlayerProbability } from '@/types/roulette';
import * as S from './RouletteWheel.styled';
import { WHEEL_CONFIG } from '../../constants/config';
import { convertProbabilitiesToAngles } from '../../utils/convertProbabilitiesToAngles';
import RouletteSlice from '../RouletteSlice/RouletteSlice';

type Props =
  | {
      angles: RouletteSector[];
      playerProbabilities?: never;
      isSpinning?: boolean;
    }
  | {
      angles?: never;
      playerProbabilities: PlayerProbability[];
      isSpinning?: boolean;
    };

const RouletteWheel = ({ angles, playerProbabilities, isSpinning = false }: Props) => {
  const theme = useTheme();

  const playersWithAngles = angles || convertProbabilitiesToAngles(playerProbabilities);

  return (
    <S.Container>
      <S.Wrapper $isSpinning={isSpinning}>
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
