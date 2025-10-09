import { useEffect, useRef } from 'react';

const ROTATION_SPEED_MULTIPLIER = 22;

type Props = {
  speed: number;
};

export const useRotationAnimation = ({ speed }: Props) => {
  const rotatingRef = useRef<HTMLDivElement>(null);
  const angleRef = useRef(0);

  useEffect(() => {
    let frameId: number;
    let lastTime = performance.now();

    const update = (time: number) => {
      const delta = (time - lastTime) / 1000; // 초 단위
      lastTime = time;

      angleRef.current += speed * delta * 10 * ROTATION_SPEED_MULTIPLIER; // 속도에 비례해 증가
      if (rotatingRef.current) {
        rotatingRef.current.style.transform = `rotate(${angleRef.current}deg)`;
      }

      frameId = requestAnimationFrame(update);
    };

    frameId = requestAnimationFrame(update);
    return () => cancelAnimationFrame(frameId);
  }, [speed]);

  return rotatingRef;
};
