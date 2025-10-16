import { useState, useEffect } from 'react';

type AnimationState = 'fadingIn' | 'fadingOut';

type UseAutoSlideCarouselOptions = {
  slideCount: number;
  displayDuration?: number;
  fadeDuration?: number;
};

export const useAutoSlideCarousel = ({
  slideCount,
  displayDuration = 4000,
  fadeDuration = 400,
}: UseAutoSlideCarouselOptions) => {
  const [currentSlideIndex, setCurrentSlideIndex] = useState(0);
  const [animationState, setAnimationState] = useState<AnimationState>('fadingIn');

  useEffect(() => {
    const slideTransitionCycle = setInterval(() => {
      setAnimationState('fadingOut');

      setTimeout(() => {
        setCurrentSlideIndex((prevIndex) => (prevIndex + 1) % slideCount);
        setAnimationState('fadingIn');
      }, fadeDuration);
    }, displayDuration);

    return () => {
      clearInterval(slideTransitionCycle);
    };
  }, [slideCount, displayDuration, fadeDuration]);

  return { currentSlideIndex, animationState };
};
