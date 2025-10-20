import LowestProbabilitySlide from './LowestProbabilitySlide/LowestProbabilitySlide';
import TopWinnersSlide from './TopWinnersSlide/TopWinnersSlide';
import GamePlayCountSlide from './GamePlayCountSlide/GamePlayCountSlide';
import { useAutoSlideCarousel } from '@/hooks/useAutoSlideCarousel';
import * as S from './DashBoard.styled';
import useFetch from '@/apis/rest/useFetch';
import type { TopWinner, LowestProbabilityWinner, GamePlayCount } from '@/types/dashBoard';

const DashBoard = () => {
  const { data: topWinners } = useFetch<TopWinner[]>({
    endpoint: '/dashboard/top-winners',
  });
  const { data: lowestProbabilityWinner } = useFetch<LowestProbabilityWinner>({
    endpoint: '/dashboard/lowest-probability-winner',
  });
  const { data: gamePlayCounts } = useFetch<GamePlayCount[]>({
    endpoint: '/dashboard/game-play-counts',
  });

  const slides = [
    {
      key: 'top3',
      component: <TopWinnersSlide winners={topWinners || []} displayCount={5} />,
    },
    {
      key: 'lowest',
      component: (
        <LowestProbabilitySlide
          WinnerNames={lowestProbabilityWinner?.playerNames || []}
          probability={lowestProbabilityWinner?.probability || 0}
        />
      ),
    },
    {
      key: 'game-play-counts',
      component: <GamePlayCountSlide games={gamePlayCounts || []} />,
    },
  ];

  const { currentSlideIndex, animationState } = useAutoSlideCarousel({
    slideCount: slides.length,
    displayDuration: 5500,
    fadeDuration: 400,
  });

  return (
    <S.CarouselContainer>
      <S.SlideWrapper
        key={`${slides[currentSlideIndex].key}-active`}
        $animationState={animationState}
      >
        {slides[currentSlideIndex].component}
      </S.SlideWrapper>
    </S.CarouselContainer>
  );
};

export default DashBoard;
