import { useEffect, useRef, useState } from 'react';
import { PlayerProbability } from '@/types/roulette';
import { interpolateAngles } from '../utils/interpolateAngles';

const ANIMATION_DURATION = 1600; // ms

// Easing function - 빠른 시작, 부드러운 끝
const easeOutCubic = (t: number): number => {
  return 1 - Math.pow(1 - t, 3);
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
      const t = easeOutCubic(rawT);

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
