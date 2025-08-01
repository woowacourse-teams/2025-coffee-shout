import Headline4 from '@/components/@common/Headline4/Headline4';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RoulettePlaySection.styled';
import { PlayerProbability } from '@/types/roulette';

type Props = {
  playerProbabilities: PlayerProbability[];
  isSpinning: boolean;
};

const RoulettePlaySection = ({ playerProbabilities, isSpinning }: Props) => {
  return (
    <S.Container>
      <RouletteWheel isSpinning={isSpinning} playerProbabilities={playerProbabilities} />
      <S.ProbabilityText>
        <Headline4>당첨 확률 +10%</Headline4>
      </S.ProbabilityText>
    </S.Container>
  );
};

export default RoulettePlaySection;
