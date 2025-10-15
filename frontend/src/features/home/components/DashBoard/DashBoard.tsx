import LowestProbabilitySlide from '@/components/@composition/LowestProbabilitySlide/LowestProbabilitySlide';
import Top3WinnersSlide from '@/components/@composition/Top3WinnersSlide/Top3WinnersSlide';
import MiniGameStatsSlide from '@/components/@composition/MiniGameStatsSlide/MiniGameStatsSlide';
import { useAutoSlideCarousel } from '@/hooks/useAutoSlideCarousel';
import * as S from './DashBoard.styled';
const slides = [
  {
    key: 'top3',
    component: (
      <Top3WinnersSlide
        winners={[
          { name: '세라', count: 20 },
          { name: '민수', count: 15 },
          { name: '지영', count: 12 },
        ]}
      />
    ),
  },
  {
    key: 'lowest',
    component: <LowestProbabilitySlide winnerName="세라" probability={0.1} />,
  },
  {
    key: 'minigame',
    component: (
      <MiniGameStatsSlide
        games={[
          { name: '카드게임', count: 20 },
          { name: '레이싱게임', count: 15 },
          { name: '룰렛', count: 8 },
        ]}
      />
    ),
  },
];

const DashBoard = () => {
  const { currentSlideIndex, animationState } = useAutoSlideCarousel({
    slideCount: slides.length,
    displayDuration: 4000,
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
