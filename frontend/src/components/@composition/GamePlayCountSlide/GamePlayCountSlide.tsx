import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import FadeInItem from '@/components/@common/FadeInItem/FadeInItem';
import { useHeightDifference } from '@/hooks/useHeightDifference';
import * as S from './GamePlayCountSlide.styled';
import type { GamePlayCount } from '@/types/dashBoard';

type Props = {
  games: GamePlayCount[];
};

const GamePlayCountSlide = ({ games }: Props) => {
  const { containerRef, wrapperRef, heightDifference } = useHeightDifference({
    fadeInOffset: 20,
    dependencies: [games],
  });

  return (
    <CarouselSlide title="미니게임 플레이 횟수">
      <S.SlideContainer ref={containerRef}>
        <S.Wrapper ref={wrapperRef} $slideDistance={heightDifference}>
          {games.map((game, index) => (
            <FadeInItem key={game.gameType} index={index}>
              <RankingItem rank={index + 1} name={game.gameType} count={game.playCount} />
            </FadeInItem>
          ))}
        </S.Wrapper>
      </S.SlideContainer>
    </CarouselSlide>
  );
};

export default GamePlayCountSlide;
