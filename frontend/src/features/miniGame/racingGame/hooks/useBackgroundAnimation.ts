import { useEffect, useRef, RefObject } from 'react';

const BACKGROUND_SPEED_MULTIPLIER = 2.5;

type Props = {
  containerRef: RefObject<HTMLDivElement | null>;
  mySpeed: number;
};

export const useBackgroundAnimation = ({ containerRef, mySpeed }: Props) => {
  const backgroundPositionRef = useRef(0);

  useEffect(() => {
    let frameId: number;
    let lastTime = performance.now();

    const update = (time: number) => {
      const delta = (time - lastTime) / 1000; // 초 단위
      lastTime = time;

      backgroundPositionRef.current += mySpeed * delta * BACKGROUND_SPEED_MULTIPLIER; // 속도에 비례해 증가
      if (containerRef.current) {
        containerRef.current.style.backgroundPosition = `${backgroundPositionRef.current}% center`;
      }

      frameId = requestAnimationFrame(update);
    };

    frameId = requestAnimationFrame(update);
    return () => cancelAnimationFrame(frameId);
  }, [mySpeed, containerRef]);

  return backgroundPositionRef;
};
