import { useRef, useEffect, useState } from 'react';
import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import * as S from './Top3WinnersSlide.styled';

type Winner = {
  name: string;
  count: number;
};

type Props = {
  winners: Winner[];
};

const Top3WinnersSlide = ({ winners }: Props) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);
  const [heightDifference, setHeightDifference] = useState(0);

  useEffect(() => {
    const calculateHeightDifference = () => {
      if (containerRef.current && wrapperRef.current) {
        const containerHeight = containerRef.current.clientHeight;
        const wrapperHeight = wrapperRef.current.scrollHeight;
        const difference = wrapperHeight - containerHeight;

        setHeightDifference(difference);
        console.log('Container height:', containerHeight);
        console.log('Wrapper height:', wrapperHeight);
        console.log('Height difference:', difference);
      }
    };

    // 초기 계산
    calculateHeightDifference();

    // 리사이즈 이벤트 리스너 추가
    window.addEventListener('resize', calculateHeightDifference);

    return () => {
      window.removeEventListener('resize', calculateHeightDifference);
    };
  }, [winners]);

  return (
    <CarouselSlide title="이번달 TOP3 당첨자">
      <S.SlideContainer ref={containerRef}>
        <S.Wrapper ref={wrapperRef} $slideDistance={heightDifference}>
          {winners.slice(0, 3).map((winner, index) => (
            <RankingItem
              key={winner.name}
              rank={index + 1}
              name={winner.name}
              count={winner.count}
            />
          ))}
        </S.Wrapper>
      </S.SlideContainer>
    </CarouselSlide>
  );
};

export default Top3WinnersSlide;
