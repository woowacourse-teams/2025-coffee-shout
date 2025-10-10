import { useEffect, useRef } from 'react';

const ROTATION_SPEED_MULTIPLIER = 22;
const SPEED_SMOOTHING_FACTOR = 0.02;

type Props = {
  speed: number;
};

export const useRotationAnimation = ({ speed }: Props) => {
  const rotatingRef = useRef<HTMLDivElement>(null);
  const angleRef = useRef(0);
  const currentSpeedRef = useRef(0);

  useEffect(() => {
    let frameId: number;
    let lastTime = performance.now();

    const update = (time: number) => {
      const delta = (time - lastTime) / 1000; // 초 단위
      lastTime = time;

      // Lerp를 사용하여 현재 속도를 목표 속도로 부드럽게 전환
      currentSpeedRef.current += (speed - currentSpeedRef.current) * SPEED_SMOOTHING_FACTOR;

      angleRef.current += currentSpeedRef.current * delta * 10 * ROTATION_SPEED_MULTIPLIER;
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
