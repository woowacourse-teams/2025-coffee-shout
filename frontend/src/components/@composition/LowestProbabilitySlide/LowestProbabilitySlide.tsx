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

const LowestProbabilitySlide = ({
  WinnerNames, // eslint-disable-line @typescript-eslint/no-unused-vars
  probability,
}: Props) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const namesRef = useRef<HTMLDivElement>(null);
  const [slideDistance, setSlideDistance] = useState(0);

  useEffect(() => {
    if (containerRef.current && namesRef.current) {
      const containerWidth = containerRef.current.clientWidth;
      const namesWidth = namesRef.current.scrollWidth;
      const distance = namesWidth - containerWidth;

      console.log('컨테이너 너비:', containerWidth);
      console.log('이름들 전체 너비:', namesWidth);
      console.log('슬라이드해야 할 길이:', distance);

      setSlideDistance(distance);
    }
  }, []);

  return (
    <CarouselSlide title="최저 확률 우승자">
      <S.Wrapper ref={containerRef}>
        <S.NamesContainer ref={namesRef}>
          <FadeInItem index={0} delay={0}>
            <S.WinnerName $slideDistance={slideDistance}>
              {[
                '세라세[라세라세',
                '다이앤이다이앤이다이',
                '니야야야야야야',
                // '야니니니니니니니',
                // '앤이다ㅇ라ㅓ렁ㄹ',
              ].map((winnerName) => (
                <Headline1 key={winnerName} color="white">
                  {winnerName}
                </Headline1>
              ))}
              {/* {WinnerNames.map((winnerName) => (
                <Headline1 key={winnerName} color="white">
                  {winnerName}
                </Headline1>
              ))} */}
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
