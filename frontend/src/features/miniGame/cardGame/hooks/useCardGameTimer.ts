import { useCardGame } from '@/contexts/CardGame/CardGameContext';
import { useEffect, useRef, useState } from 'react';

const ROUND_TOTAL_TIME = 10;

export const useCardGameTimer = () => {
  const { currentCardGameState, currentRound } = useCardGame();
  const [currentTime, setCurrentTime] = useState(ROUND_TOTAL_TIME);
  const [isTimerActive, setIsTimerActive] = useState(false);
  const isTimerReset = useRef(false);

  useEffect(() => {
    if (isTimerActive && currentTime > 0) {
      const timer = setTimeout(() => setCurrentTime((prev) => prev - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [currentTime, isTimerActive]);

  useEffect(() => {
    if (currentCardGameState === 'PREPARE') {
      setCurrentTime(ROUND_TOTAL_TIME);
      setIsTimerActive(false);
      return;
    }

    if (currentCardGameState === 'PLAYING') {
      if (currentRound === 'FIRST') {
        setCurrentTime(ROUND_TOTAL_TIME);
        setIsTimerActive(true);
        isTimerReset.current = false;
        return;
      }

      if (currentRound === 'SECOND' && !isTimerReset.current) {
        setCurrentTime(ROUND_TOTAL_TIME);
        setIsTimerActive(true);
        isTimerReset.current = true;
      }
    }
  }, [currentRound, currentCardGameState]);

  return {
    currentTime,
    isTimerActive,
    roundTotalTime: ROUND_TOTAL_TIME,
  };
};
