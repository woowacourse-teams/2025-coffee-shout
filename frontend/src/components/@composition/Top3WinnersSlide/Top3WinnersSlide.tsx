import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';
import { useHeightDifference } from '@/hooks/useHeightDifference';
import * as S from './Top3WinnersSlide.styled';

type Winner = {
  name: string;
  count: number;
};

type Props = {
  winners: Winner[];
};

const Top3WinnersSlide = ({ winners }: Props) => {
  const { containerRef, wrapperRef, heightDifference } = useHeightDifference({
    fadeInOffset: 20,
    dependencies: [winners],
  });

  return (
    <CarouselSlide title="이번달 TOP3 당첨자">
      <S.SlideContainer ref={containerRef}>
        <S.Wrapper ref={wrapperRef} $slideDistance={heightDifference}>
          {winners.slice(0, 3).map((winner, index) => (
            <FadeInItem key={winner.name} index={index}>
              <RankingItem rank={index + 1} name={winner.name} count={winner.count} />
            </FadeInItem>
          ))}
        </S.Wrapper>
      </S.SlideContainer>
    </CarouselSlide>
  );
};

export default Top3WinnersSlide;
