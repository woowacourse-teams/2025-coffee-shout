import RouletteWheelIcon from '@/assets/profile-red.svg';
import { colorList } from '@/constants/color';
import { useTheme } from '@emotion/react';
import { describeArc } from '../../utils/describeArc';
import * as S from './RouletteWheel.styled';
import { PlayerProbability } from '@/types/roulette';
import { getPlayersWithAngles } from '../../utils/getPlayerWithAngles.ts';

type Angle = { playerName: string; startAngle: number; endAngle: number };

type Props =
  | { angles: Angle[]; playerProbabilities?: never; isSpinning?: boolean }
  | { playerProbabilities: PlayerProbability[]; angles?: never; isSpinning?: boolean };

const RouletteWheel = ({ angles, playerProbabilities, isSpinning = false }: Props) => {
  const theme = useTheme();

  if (angles) {
    return (
      <S.Container>
        <S.Wrapper $isSpinning={isSpinning}>
          <svg width={300} height={300} viewBox="0 0 300 300">
            {angles.map((player, index) => (
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
  }

  if (playerProbabilities) {
    const totalProbability = playerProbabilities.reduce(
      (sum, player) => sum + player.probability,
      0
    );
    const playersWithAngles = getPlayersWithAngles(playerProbabilities, totalProbability);

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
              />
            ))}
          </svg>
        </S.Wrapper>
      </S.Container>
    );
  }
};

export default RouletteWheel;
