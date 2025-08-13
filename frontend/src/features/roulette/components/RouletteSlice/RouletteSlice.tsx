import { RouletteSector } from '@/types/roulette';
import * as S from './RouletteSlice.styled';
import { WHEEL_CONFIG } from '../../constants/config';
import { getCenterAngle } from '../../utils/getCenterAngle';
import { getTextPosition } from '../../utils/getTextPosition';
import { describeArc } from '../../utils/describeArc';

type Props = {
  player: RouletteSector;
  strokeColor: string;
};

const RouletteSlice = ({ player, strokeColor }: Props) => {
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

export default RouletteSlice;
