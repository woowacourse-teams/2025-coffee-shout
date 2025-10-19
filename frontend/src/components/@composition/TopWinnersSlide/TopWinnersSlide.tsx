import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import FadeInUpList from '@/components/@composition/FadeInUpList/FadeInUpList';
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
    <CarouselSlide title={`이번달 TOP${displayCount} 당첨자`}>
      <S.SlideContainer ref={containerRef}>
        <S.Wrapper ref={wrapperRef} $slideDistance={heightDifference}>
          <FadeInUpList
            items={winners.slice(0, displayCount)}
            renderItem={(winner, index) => (
              <RankingItem rank={index + 1} name={winner.playerName} count={winner.winCount} />
            )}
          />
        </S.Wrapper>
      </S.SlideContainer>
    </CarouselSlide>
  );
};

export default TopWinnersSlide;
