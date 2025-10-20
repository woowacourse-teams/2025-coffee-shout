import { useEffect, useRef, useState } from 'react';

const GOAL_DISPLAY_DURATION_MS = 1000;

type Props = {
  myPosition: number;
  endDistance: number;
};

export const useGoalDisplay = ({ myPosition, endDistance }: Props) => {
  const [showGoal, setShowGoal] = useState(false);
  const hasShownGoalRef = useRef(false);
  const goalTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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
    return () => {
      if (goalTimerRef.current) {
        clearTimeout(goalTimerRef.current);
      }
    };
  }, [myPosition, endDistance]);

  return showGoal;
};
