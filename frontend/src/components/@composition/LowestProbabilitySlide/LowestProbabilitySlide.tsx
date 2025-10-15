import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import ProbabilityTag from '@/components/@common/ProbabilityTag/ProbabilityTag';
import Headline1 from '@/components/@common/Headline1/Headline1';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';
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
        <S.ProbabilityWrapper>
          <FadeInItem index={1} delay={500}>
            <ProbabilityTag probability={probability} />
          </FadeInItem>
        </S.ProbabilityWrapper>
      </S.Wrapper>
    </CarouselSlide>
  );
};

export default LowestProbabilitySlide;
