import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import ProbabilityTag from '@/components/@common/ProbabilityTag/ProbabilityTag';
import Headline1 from '@/components/@common/Headline1/Headline1';
import * as S from './LowestProbabilitySlide.styled';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';
import { useWidthDifference } from '@/hooks/useWidthDifference';

type Props = {
  WinnerNames: string[];
  probability: number;
};

const LowestProbabilitySlide = ({ WinnerNames, probability }: Props) => {
  const { containerRef, wrapperRef: namesRef, slideDistance } = useWidthDifference();

  return (
    <CarouselSlide title="최저 확률 우승자">
      <S.Wrapper ref={containerRef}>
        <S.NamesContainer ref={namesRef}>
          <FadeInItem index={0} delay={0}>
            <S.WinnerName $slideDistance={slideDistance}>
              {WinnerNames.map((winnerName) => (
                <Headline1 key={winnerName} color="white">
                  {winnerName}
                </Headline1>
              ))}
            </S.WinnerName>
          </FadeInItem>
        </S.NamesContainer>
        <FadeInItem index={1} delay={400}>
          <S.ProbabilityWrapper>
            <ProbabilityTag probability={probability} />
          </S.ProbabilityWrapper>
        </FadeInItem>
      </S.Wrapper>
    </CarouselSlide>
  );
};

export default LowestProbabilitySlide;
