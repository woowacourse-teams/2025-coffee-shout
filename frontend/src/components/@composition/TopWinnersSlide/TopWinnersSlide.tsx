import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';
import { useHeightDifference } from '@/hooks/useHeightDifference';
import * as S from './TopWinnersSlide.styled';
import type { TopWinner } from '@/types/dashBoard';

type Props = {
  winners: TopWinner[];
  displayCount?: number;
};

const TopWinnersSlide = ({ winners, displayCount = 3 }: Props) => {
  const { containerRef, wrapperRef, heightDifference } = useHeightDifference({
    fadeInOffset: 20,
    dependencies: [winners],
  });

  return (
    <CarouselSlide title="이번달 TOP3 당첨자">
      <S.SlideContainer ref={containerRef}>
        <S.Wrapper ref={wrapperRef} $slideDistance={heightDifference}>
          {winners.slice(0, displayCount).map((winner, index) => (
            <FadeInItem key={winner.nickname} index={index}>
              <RankingItem rank={index + 1} name={winner.nickname} count={winner.winCount} />
            </FadeInItem>
          ))}
        </S.Wrapper>
      </S.SlideContainer>
    </CarouselSlide>
  );
};

export default TopWinnersSlide;
