import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTransition';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { convertProbabilitiesToAngles } from '@/features/roulette/utils/convertProbabilitiesToAngles';
import { Angle } from '@/types/roulette';

type Props = {
  isSpinning: boolean;
  winner: string | null;
};

const RoulettePlaySection = ({ isSpinning, winner }: Props) => {
  const { myName } = useIdentifier();
  const { probabilityHistory } = useProbabilityHistory();
  const angles = useRouletteTransition(probabilityHistory.prev, probabilityHistory.current);

  const myPrevProbability =
    probabilityHistory.prev.find((player) => player.playerName === myName)?.probability ?? 0;
  const myCurrentProbability =
    probabilityHistory.current.find((player) => player.playerName === myName)?.probability ?? 0;

  const myProbabilityChange = myCurrentProbability - myPrevProbability;

  const shouldComputeFinalRotation = isSpinning && winner;
  const finalRotation = shouldComputeFinalRotation
    ? calculateFinalRotation(convertProbabilitiesToAngles(probabilityHistory.current), winner)
    : 0;

  if (!angles) return null;

  return (
    <S.Container>
      <RouletteWheel isSpinning={isSpinning} angles={angles} finalRotation={finalRotation} />
      <S.ProbabilityText>
        <Headline4>
          당첨 확률 {myProbabilityChange >= 0 ? '+' : ''}
          {myProbabilityChange}%
        </Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;

const calculateFinalRotation = (finalAngles: Angle[], winner: string | null) => {
  if (!winner) return 0;
  const winnerData = finalAngles.find((player) => player.playerName === winner);
  if (!winnerData) return 0;

  // 당첨자 영역의 중앙 각도
  const winnerCenterAngle = (winnerData.startAngle + winnerData.endAngle) / 2;

  // 12시 방향에 맞추기 위한 회전 각도
  let finalRotation = 360 - winnerCenterAngle;

  // 회전 각도를 0 ~ 360 범위로 정규화
  while (finalRotation < 0) {
    finalRotation += 360;
  }
  while (finalRotation >= 360) {
    finalRotation -= 360;
  }

  return finalRotation;
};
