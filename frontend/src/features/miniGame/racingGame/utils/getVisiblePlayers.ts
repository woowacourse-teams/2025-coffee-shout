// utils/getVisiblePlayers.ts
type Player = {
  playerName: string;
  speed: number;
  x: number;
};

// 상수 정의
const VISIBLE_PLAYER_COUNT = 7; // 위 3명 + 나 + 아래 3명
const HALF_COUNT = 3; // 위/아래 각각 3명

/**
 * 플레이어 배열을 순환배열처럼 자르는 함수
 * 내 위로 3명, 나, 내 아래로 3명 (총 7명)을 반환
 * @param players 전체 플레이어 배열
 * @param myIndex 내 플레이어의 인덱스
 * @returns 잘린 플레이어 배열 (내가 중앙)
 */
export const getVisiblePlayers = (players: Player[], myName: string): Player[] => {
  if (players.length === 0) return [];

  const totalPlayers = players.length;
  const myIndex = players.findIndex((player) => player.playerName === myName);

  // 플레이어가 7명 이하면 원본 배열 길이 유지하되 내가 중앙에 오도록 정렬
  if (totalPlayers <= VISIBLE_PLAYER_COUNT) {
    const result: Player[] = [];

    for (let i = 0; i < totalPlayers; i++) {
      const offset = i - Math.floor(totalPlayers / 2); // 중앙 기준으로 offset 계산
      const playerIndex = (myIndex + offset + totalPlayers) % totalPlayers;
      result.push({ ...players[playerIndex] }); // 얕은 복사
    }

    return result;
  }

  // 7명 초과일 때는 기존 로직 (7명만 반환)
  const result: Player[] = [];

  for (let i = 0; i < VISIBLE_PLAYER_COUNT; i++) {
    const offset = i - HALF_COUNT; // -3, -2, -1, 0, 1, 2, 3
    const playerIndex = (myIndex + offset + totalPlayers) % totalPlayers;
    result.push({ ...players[playerIndex] }); // 얕은 복사
  }

  return result;
};
