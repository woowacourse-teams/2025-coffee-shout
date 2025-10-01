import { useEffect, useRef, useState } from 'react';

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
  { playerName: '홍길동', speed: 40 },
  { playerName: '김철수', speed: 40 },
  { playerName: '망고맛아이스티한잔', speed: 40 },
  { playerName: 'welrkjgfhh', speed: 40 },
  { playerName: '정민수', speed: 40 },
  { playerName: '최지영', speed: 40 },
  { playerName: '강동원', speed: 40 },
  { playerName: '윤서연', speed: 40 },
  { playerName: '임태현', speed: 40 },
];

const DISTANCE_END = 1000;
const UPDATE_INTERVAL_MS = 100;
const READY_DURATION_MS = 2000;
const FINISH_DURATION_MS = 2000;
const DECELERATION_START_DISTANCE = DISTANCE_END; // 1000부터 감속 시작
const SPEED_REDUCTION_RATE = 0.92; // 매 프레임마다 속도의 92%로 감속
const SPEED_INCREASE_RATE = 2; // 매 프레임마다 속도의 200%로 가속 (빠른 가속)
const SPEED_VARIATION_RATE = 0.8; // 속도 변동 폭 (±80%)
const SPEED_VARIATION_INTERVAL = 30; // 30프레임(3초)마다 속도 변동

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
  const frameCountRef = useRef(0);

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
      frameCountRef.current = 0;
      return;
    }

    const interval = setInterval(() => {
      frameCountRef.current += 1;
      const shouldUpdateSpeed = frameCountRef.current % SPEED_VARIATION_INTERVAL === 0;

      setRacingGameData((prevData) => {
        const updatedPlayers = prevData.players.map((player) => {
          const initialPlayer = INITIAL_PLAYERS.find((p) => p.playerName === player.playerName);
          const targetSpeed = initialPlayer?.speed ?? 0;

          let newSpeed = player.speed;
          const isInDecelerationZone = player.x >= DECELERATION_START_DISTANCE;

          if (isInDecelerationZone) {
            // 감속 구간(x >= 1000)에서 서서히 속도 감소
            newSpeed = player.speed * SPEED_REDUCTION_RATE;
            // 속도가 매우 작아지면 0으로 설정
            if (newSpeed < 0.1) {
              newSpeed = 0;
            }
          } else if (player.speed < targetSpeed) {
            // 목표 속도에 도달하지 않았으면 서서히 가속
            const baseSpeed = player.speed === 0 ? 0.5 : player.speed;
            newSpeed = Math.min(baseSpeed * SPEED_INCREASE_RATE, targetSpeed);
          } else if (shouldUpdateSpeed) {
            // 1초마다 목표 속도 기준 랜덤 변동 적용 (추월 가능)
            const variation = (Math.random() - 0.5) * 2 * SPEED_VARIATION_RATE;
            const speedWithVariation = targetSpeed * (1 + variation);
            newSpeed = Math.max(speedWithVariation, targetSpeed * 0.2); // 최소 20%는 유지
          }
          // shouldUpdateSpeed가 false면 기존 speed 유지

          const newX = player.x + newSpeed / 10;

          return {
            ...player,
            x: newX,
            speed: newSpeed,
          };
        });

        // 모든 플레이어가 멈췄는지 확인 (속도가 0)
        const allPlayersFinished = updatedPlayers.every((player) => player.speed === 0);

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
