import { useEffect, useState } from 'react';

type RacingGameState = 'READY' | 'PLAYING' | 'FINISH';

type RacingGameData = {
  distance: {
    start: number;
    end: number;
  };
  players: Array<{
    playerName: string;
    x: number;
    speed: number;
  }>;
};

const INITIAL_PLAYERS = [
  { playerName: '홍길동', speed: 10 },
  { playerName: '김철수', speed: 10 },
  { playerName: '이순신', speed: 20 },
  { playerName: '박영희', speed: 20 },
  { playerName: '정민수', speed: 40 },
  { playerName: '최지영', speed: 60 },
  { playerName: '강동원', speed: 30 },
  { playerName: '윤서연', speed: 10 },
  { playerName: '임태현', speed: 10 },
];

const DISTANCE_END = 1000;
const UPDATE_INTERVAL_MS = 100;
const READY_DURATION_MS = 2000;
const FINISH_DURATION_MS = 2000;
const DECELERATION_START_DISTANCE = DISTANCE_END * 0.9; // 900부터 감속 시작
const SPEED_REDUCTION_RATE = 0.92; // 매 프레임마다 속도의 92%로 감속
const SPEED_INCREASE_RATE = 1.08; // 매 프레임마다 속도의 108%로 가속

export const useRacingGameMock = () => {
  const [racingGameState, setRacingGameState] = useState<RacingGameState>('READY');
  const [racingGameData, setRacingGameData] = useState<RacingGameData>({
    distance: {
      start: 0,
      end: DISTANCE_END,
    },
    players: INITIAL_PLAYERS.map((player) => ({
      ...player,
      x: 0,
      speed: 0,
    })),
  });

  // READY 상태에서 PLAYING으로 전환
  useEffect(() => {
    if (racingGameState !== 'READY') {
      return;
    }

    const timer = setTimeout(() => {
      setRacingGameState('PLAYING');
    }, READY_DURATION_MS);

    return () => {
      clearTimeout(timer);
    };
  }, [racingGameState]);

  // FINISH 상태에서 READY로 전환
  useEffect(() => {
    if (racingGameState !== 'FINISH') {
      return;
    }

    const timer = setTimeout(() => {
      setRacingGameState('READY');
    }, FINISH_DURATION_MS);

    return () => {
      clearTimeout(timer);
    };
  }, [racingGameState]);

  // 상태 변경에 따른 데이터 초기화
  useEffect(() => {
    if (racingGameState === 'READY') {
      setRacingGameData({
        distance: {
          start: 0,
          end: DISTANCE_END,
        },
        players: INITIAL_PLAYERS.map((player) => ({
          ...player,
          x: 0,
          speed: 0,
        })),
      });
    }
    // PLAYING 상태로 전환 시 speed는 0에서 시작 (서서히 증가)
  }, [racingGameState]);

  // PLAYING 상태에서 위치 업데이트 및 완주 확인
  useEffect(() => {
    if (racingGameState !== 'PLAYING') {
      return;
    }

    const interval = setInterval(() => {
      setRacingGameData((prevData) => {
        const updatedPlayers = prevData.players.map((player) => {
          const initialPlayer = INITIAL_PLAYERS.find((p) => p.playerName === player.playerName);
          const targetSpeed = initialPlayer?.speed ?? 0;

          const newX = Math.min(player.x + player.speed / 10, DISTANCE_END);
          const hasReachedEnd = newX >= DISTANCE_END;
          const isNearEnd = newX >= DECELERATION_START_DISTANCE;

          let newSpeed = player.speed;

          if (hasReachedEnd) {
            // 결승선 도달 시 속도 0
            newSpeed = 0;
          } else if (isNearEnd) {
            // 감속 구간에서 서서히 속도 감소
            newSpeed = player.speed * SPEED_REDUCTION_RATE;
          } else if (player.speed < targetSpeed) {
            // 목표 속도에 도달하지 않았으면 서서히 가속
            const baseSpeed = player.speed === 0 ? 0.5 : player.speed;
            newSpeed = Math.min(baseSpeed * SPEED_INCREASE_RATE, targetSpeed);
          }

          return {
            ...player,
            x: newX,
            speed: newSpeed,
          };
        });

        // 모든 플레이어가 결승선에 도달했는지 확인
        const allPlayersFinished = updatedPlayers.every((player) => player.x >= DISTANCE_END);

        if (allPlayersFinished) {
          setRacingGameState('FINISH');
        }

        return {
          ...prevData,
          players: updatedPlayers,
        };
      });
    }, UPDATE_INTERVAL_MS);

    return () => {
      clearInterval(interval);
    };
  }, [racingGameState]);

  return { racingGameState, racingGameData };
};
