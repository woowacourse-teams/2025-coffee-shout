import { useTheme } from '@emotion/react';
import { describeArc } from '../../utils/describeArc';
import { Angle, PlayerProbability } from '@/types/roulette';
import * as S from './RouletteWheel.styled';
import { getCenterAngle } from '../../utils/getCenterAngle';
import { getTextPosition } from '../../utils/getTextPosition';
import { WHEEL_CONFIG } from '../../constants/config';
import { convertProbabilitiesToAngles } from '../../utils/convertProbabilitiesToAngles';

type Props =
  | {
      angles: Angle[];
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

type RouletteSliceProps = {
  player: { playerName: string; playerColor: string; startAngle: number; endAngle: number };
  strokeColor: string;
};

const RouletteSlice = ({ player, strokeColor }: RouletteSliceProps) => {
  const centerAngle = getCenterAngle(player.startAngle, player.endAngle);
  const textPosition = getTextPosition(centerAngle);

  return (
    <g key={player.playerName}>
      <path
        d={describeArc({
          cx: WHEEL_CONFIG.CENTER,
          cy: WHEEL_CONFIG.CENTER,
          r: WHEEL_CONFIG.RADIUS,
          startAngle: player.startAngle,
          endAngle: player.endAngle,
        })}
        fill={player.playerColor}
        stroke={strokeColor}
        strokeWidth={WHEEL_CONFIG.STROKE_WIDTH}
      />
      <S.PlayerNameText
        x={textPosition.x}
        y={textPosition.y}
        textAnchor="middle"
        dominantBaseline="middle"
      >
        {player.playerName}
      </S.PlayerNameText>
    </g>
  );
};
