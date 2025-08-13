import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { useRouletteTransition } from '@/features/roulette/hooks/useRouletteTransition';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props = {
  isSpinning: boolean;
};

const RoulettePlaySection = ({ isSpinning }: Props) => {
  const { myName } = useIdentifier();
  const { probabilityHistory } = useProbabilityHistory();
  const animatedSectors = useRouletteTransition(
    probabilityHistory.prev,
    probabilityHistory.current
  );

  const myPrevProbability =
    probabilityHistory.prev.find((player) => player.playerName === myName)?.probability ?? 0;
  const myCurrentProbability =
    probabilityHistory.current.find((player) => player.playerName === myName)?.probability ?? 0;

  const myProbabilityChange = myCurrentProbability - myPrevProbability;

  if (!animatedSectors) return null;

  return (
    <S.Container>
      <RouletteWheel isSpinning={isSpinning} sectors={animatedSectors} />
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
