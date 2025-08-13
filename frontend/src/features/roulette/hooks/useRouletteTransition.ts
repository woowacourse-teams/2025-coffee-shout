import { useEffect, useRef, useState } from 'react';
import { RouletteSector, PlayerProbability } from '@/types/roulette';
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
  // 현재 애니메이션 중인 각도 상태
  const [angles, setAngles] = useState<RouletteSector[] | null>(null);

  const requestRef = useRef<number>(null);
  const startTimeRef = useRef<number | null>(null);

  useEffect(() => {
    if (!prev || !current || prev.length === 0 || current.length === 0) {
      setAngles(null);
      return;
    }

    //requestAnimationFrame이 호출할 프레임 처리 함수
    const step = (timestamp: number) => {
      // 첫 프레임에서 시작 시간 설정
      if (startTimeRef.current === null) startTimeRef.current = timestamp;
      // 애니메이션 시작부터 경과된 시간 계산
      const elapsed = timestamp - startTimeRef.current;
      // 진행률 계산 (0 ~ 1)
      const rawT = Math.min(elapsed / ANIMATION_DURATION, 1);

      // Easing 적용, t는 애니메이션 진행률을 나타냄
      const progress = easeOutCubic(rawT);

      // 현재 진행률에 따른 중간 각도 계산
      const next = interpolateAngles({ from: prev, to: current, progress });
      setAngles(next);

      // 애니메이션이 완료되지 않았다면 다음 프레임 요청
      if (rawT < 1) {
        requestRef.current = requestAnimationFrame(step);
      } else {
        // 애니메이션 완료: 상태 정리
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
