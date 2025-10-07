import { useEffect, useRef, useState } from 'react';

const GOAL_DISPLAY_DURATION_MS = 1000;

type Props = {
  myPosition: number;
  endDistance: number;
  gameState: string;
};

export const useGoalDisplay = ({ myPosition, endDistance, gameState }: Props) => {
  const [showGoal, setShowGoal] = useState(false);
  const hasShownGoalRef = useRef(false);
  const goalTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // READY 상태에서 Goal 표시 여부 리셋
  useEffect(() => {
    if (gameState === 'READY') {
      hasShownGoalRef.current = false;
      setShowGoal(false);
      if (goalTimerRef.current) {
        clearTimeout(goalTimerRef.current);
        goalTimerRef.current = null;
      }
    }
  }, [gameState]);

  // Goal 도달 감지 및 1초 표시
  useEffect(() => {
    const hasReachedGoal = myPosition >= endDistance;

    if (hasReachedGoal && !hasShownGoalRef.current && !goalTimerRef.current) {
      hasShownGoalRef.current = true;
      setShowGoal(true);

      goalTimerRef.current = setTimeout(() => {
        setShowGoal(false);
        goalTimerRef.current = null;
      }, GOAL_DISPLAY_DURATION_MS);
    }
  }, [myPosition, endDistance]);

  return showGoal;
};
