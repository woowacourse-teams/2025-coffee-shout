import { useTheme } from '@emotion/react';
import { describeArc } from '../../utils/describeArc';
import { getPlayersWithAngles } from '../../utils/getPlayerWithAngles.ts';
import { Angle, PlayerProbability } from '@/types/roulette';
import * as S from './RouletteWheel.styled';
import { getCenterAngle } from '../../utils/getCenterAngle';
import { getTextPosition } from '../../utils/getTextPosition';

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

  if (angles) {
    return (
      <S.Container>
        <S.Wrapper $isSpinning={isSpinning}>
          <svg width={300} height={300} viewBox="0 0 300 300">
            {angles.map((player) => {
              const centerAngle = getCenterAngle(player.startAngle, player.endAngle);
              const textPosition = getTextPosition(centerAngle);

              return (
                <g key={player.playerName}>
                  <path
                    d={describeArc({
                      cx: 150,
                      cy: 150,
                      r: 140,
                      startAngle: player.startAngle,
                      endAngle: player.endAngle,
                    })}
                    fill={player.playerColor}
                    stroke={theme.color.point[100]}
                    strokeWidth="1"
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
            })}
          </svg>
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
            {playersWithAngles.map((player) => {
              const centerAngle = getCenterAngle(player.startAngle, player.endAngle);
              const textPosition = getTextPosition(centerAngle);

              return (
                <g key={player.playerName}>
                  <path
                    d={describeArc({
                      cx: 150,
                      cy: 150,
                      r: 140,
                      startAngle: player.startAngle,
                      endAngle: player.endAngle,
                    })}
                    fill={player.playerColor}
                    stroke={theme.color.point[100]}
                  />
                  <S.PlayerNameText
                    x={textPosition.x}
                    y={textPosition.y}
                    style={{
                      textAnchor: 'middle',
                      dominantBaseline: 'middle',
                    }}
                  >
                    {player.playerName}
                  </S.PlayerNameText>
                </g>
              );
            })}
          </svg>
        </S.Wrapper>
      </S.Container>
    );
  }
};

export default RouletteWheel;
