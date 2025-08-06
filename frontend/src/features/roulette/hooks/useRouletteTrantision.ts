import { useEffect, useRef, useState } from 'react';
import { PlayerProbability } from '@/types/roulette';
import { interpolateAngles } from '../utils/interpolateAngles';

const ANIMATION_DURATION = 2000; // ms

// Easing function - 부드러운 시작과 끝
const easeInOutCubic = (t: number): number => {
  return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
};

export const useRouletteTransition = (
  prev: PlayerProbability[] | null,
  current: PlayerProbability[] | null
) => {
  const [angles, setAngles] = useState<
    { playerName: string; startAngle: number; endAngle: number }[] | null
  >(null);

  const requestRef = useRef<number>(null);
  const startTimeRef = useRef<number | null>(null);

  useEffect(() => {
    if (!prev || !current || prev.length === 0 || current.length === 0) {
      setAngles(null);
      return;
    }

    const step = (timestamp: number) => {
      if (startTimeRef.current === null) startTimeRef.current = timestamp;
      const elapsed = timestamp - startTimeRef.current;
      const rawT = Math.min(elapsed / ANIMATION_DURATION, 1);

      // Easing 적용
      const t = easeInOutCubic(rawT);

      const next = interpolateAngles({ from: prev, to: current, t });
      setAngles(next);

      if (rawT < 1) {
        requestRef.current = requestAnimationFrame(step);
      } else {
        startTimeRef.current = null;
        cancelAnimationFrame(requestRef.current!);
      }
    };

    requestRef.current = requestAnimationFrame(step);

    return () => {
      cancelAnimationFrame(requestRef.current!);
      startTimeRef.current = null;
    };
  }, [prev, current]);

  return angles;
};
