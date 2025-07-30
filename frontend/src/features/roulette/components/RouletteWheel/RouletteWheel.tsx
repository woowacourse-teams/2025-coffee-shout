import { useTheme } from '@emotion/react';
import RouletteWheelIcon from '@/assets/profile-red.svg';
import * as S from './RouletteWheel.styled';
import { describeArc } from '../../utils/describeArc';
import { colorList } from '@/constants/color';
import { getPlayersWithAngles } from '../../utils/getPlayerWithAngles.ts';

type Player = {
  playerName: string;
  probability: number;
};

type Props = {
  players: Player[];
  isSpinning?: boolean;
};

const RouletteWheel = ({ players, isSpinning = false }: Props) => {
  const theme = useTheme();
  const totalProbability = players.reduce((sum, player) => sum + player.probability, 0);

  const playersWithAngles = getPlayersWithAngles(players, totalProbability);

  return (
    <S.Container>
      <S.Wrapper $isSpinning={isSpinning}>
        <svg width={300} height={300} viewBox="0 0 300 300">
          {playersWithAngles.map((player, index) => (
            <path
              key={player.playerName}
              d={describeArc({
                cx: 150,
                cy: 150,
                r: 140,
                startAngle: player.startAngle,
                endAngle: player.endAngle,
              })}
              fill={colorList[index % colorList.length]}
              stroke={theme.color.point[100]}
              strokeWidth="1"
            />
          ))}
        </svg>
        <S.CenterImage src={RouletteWheelIcon} alt="roulette-center" />
      </S.Wrapper>
    </S.Container>
  );
};

export default RouletteWheel;
