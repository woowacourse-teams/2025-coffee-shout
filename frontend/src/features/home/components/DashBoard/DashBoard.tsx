import { useState, useEffect } from 'react';
import LowestProbabilitySlide from '@/components/@composition/LowestProbabilitySlide/LowestProbabilitySlide';
import Top3WinnersSlide from '@/components/@composition/Top3WinnersSlide/Top3WinnersSlide';
import MiniGameStatsSlide from '@/components/@composition/MiniGameStatsSlide/MiniGameStatsSlide';
import * as S from './DashBoard.styled';

const DashBoard = () => {
  const [currentSlideIndex, setCurrentSlideIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);

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

  useEffect(() => {
    const timer = setInterval(() => {
      setIsTransitioning(true);

      // FadeOut 애니메이션 후 슬라이드 변경
      setTimeout(() => {
        setCurrentSlideIndex((prevIndex) => (prevIndex + 1) % slides.length);
        setIsTransitioning(false);
      }, 800); // FadeOut 시간
    }, 5000);

    return () => clearInterval(timer);
  }, [slides.length]);

  return (
    <S.CarouselContainer>
      <S.SlideWrapper
        key={`${slides[currentSlideIndex].key}-active`}
        $isTransitioning={isTransitioning}
      >
        {slides[currentSlideIndex].component}
      </S.SlideWrapper>
    </S.CarouselContainer>
  );
};

export default DashBoard;
