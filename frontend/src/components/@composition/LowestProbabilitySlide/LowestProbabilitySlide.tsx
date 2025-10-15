import { useEffect, useRef, useState } from 'react';
import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import ProbabilityTag from '@/components/@common/ProbabilityTag/ProbabilityTag';
import Headline1 from '@/components/@common/Headline1/Headline1';
import * as S from './LowestProbabilitySlide.styled';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';

type Props = {
  WinnerNames: string[];
  probability: number;
};

const LowestProbabilitySlide = ({ WinnerNames, probability }: Props) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const namesRef = useRef<HTMLDivElement>(null);
  const [slideDistance, setSlideDistance] = useState(0);

  useEffect(() => {
    if (containerRef.current && namesRef.current) {
      const containerWidth = containerRef.current.clientWidth;
      const namesWidth = namesRef.current.scrollWidth;
      const distance = namesWidth - containerWidth;
      setSlideDistance(distance);
    }
  }, []);

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
