import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import ProbabilityTag from '@/components/@common/ProbabilityTag/ProbabilityTag';
import Headline1 from '@/components/@common/Headline1/Headline1';
import * as S from './LowestProbabilitySlide.styled';

type Props = {
  winnerName: string;
  probability: number;
};

const LowestProbabilitySlide = ({ winnerName, probability }: Props) => {
  return (
    <CarouselSlide title="최저 확률 우승자">
      <S.Wrapper>
        <S.WinnerName>
          <Headline1 color="white">{winnerName}</Headline1>
        </S.WinnerName>
        <ProbabilityTag probability={probability} />
      </S.Wrapper>
    </CarouselSlide>
  );
};

export default LowestProbabilitySlide;
